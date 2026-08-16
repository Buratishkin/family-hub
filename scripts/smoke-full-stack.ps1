param(
    [int]$StartupTimeoutSeconds = 180
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot "env.ps1")
$processes = @()

function New-LogPath {
    param(
        [string]$RelativePath
    )
    return Join-Path $repoRoot $RelativePath
}

function Wait-HttpOk {
    param(
        [string]$Name,
        [string]$Url,
        [int]$TimeoutSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastError = $null
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -eq 200) {
                Write-Host "OK: $Name is healthy at $Url"
                return
            }
        } catch {
            $lastError = $_
            Start-Sleep -Seconds 2
        }
    }

    throw "Timed out waiting for $Url. Last error: $lastError"
}

function Invoke-RetryJsonPost {
    param(
        [string]$Url,
        [string]$AccessToken,
        [string]$Body,
        [string]$ExpectedTarget,
        [scriptblock]$IsReady,
        [int]$TimeoutSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastResponse = $null
    $lastError = $null
    while ((Get-Date) -lt $deadline) {
        try {
            $headers = @{}
            if ($AccessToken) {
                $headers.Authorization = "Bearer $AccessToken"
            }
            $response = Invoke-WebRequest `
                -Method Post `
                -Uri $Url `
                -Headers $headers `
                -ContentType "application/json" `
                -Body $Body `
                -UseBasicParsing

            $lastResponse = $response.Content
            if ($ExpectedTarget -and $response.Headers["X-FamilyHub-Gateway-Target"] -ne $ExpectedTarget) {
                throw "Expected gateway target $ExpectedTarget, got $($response.Headers["X-FamilyHub-Gateway-Target"])"
            }

            $json = $response.Content | ConvertFrom-Json
            if (& $IsReady $json) {
                return $response
            }
        } catch {
            $lastError = $_
        }
        Start-Sleep -Seconds 2
    }

    throw "Timed out waiting for successful POST $Url. Last response: $lastResponse. Last error: $lastError"
}

function Start-ServiceProcess {
    param(
        [string[]]$ArgumentList,
        [string]$OutLog,
        [string]$ErrLog
    )

    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $OutLog) | Out-Null
    $process = Start-Process -FilePath ".\mvnw.cmd" `
        -ArgumentList $ArgumentList `
        -WorkingDirectory $repoRoot `
        -WindowStyle Hidden `
        -RedirectStandardOutput $OutLog `
        -RedirectStandardError $ErrLog `
        -PassThru
    $script:processes += $process
}

try {
    Push-Location $repoRoot
    Import-FamilyHubEnv -RepoRoot $repoRoot
    Assert-FamilyHubEnv

    if ([System.Environment]::OSVersion.Platform -eq "Win32NT" `
            -and -not (Test-Path "\\.\pipe\dockerDesktopLinuxEngine") `
            -and -not (Test-Path "\\.\pipe\docker_engine")) {
        throw "Docker daemon pipe was not found. Start Docker Desktop and rerun this script."
    }

    docker compose up -d db auth-db family-db address-db task-db meal-db kafka
    Write-Host "OK: Docker infrastructure is up: db, auth-db, family-db, address-db, task-db, meal-db, kafka"

    Start-ServiceProcess `
        -ArgumentList @("-f", "auth-service\pom.xml", "spring-boot:run") `
        -OutLog (New-LogPath "auth-service\target\smoke-full-auth.out.log") `
        -ErrLog (New-LogPath "auth-service\target\smoke-full-auth.err.log")
    Start-ServiceProcess `
        -ArgumentList @("-f", "family-service\pom.xml", "spring-boot:run") `
        -OutLog (New-LogPath "family-service\target\smoke-full-family.out.log") `
        -ErrLog (New-LogPath "family-service\target\smoke-full-family.err.log")
    Start-ServiceProcess `
        -ArgumentList @("-f", "address-service\pom.xml", "spring-boot:run") `
        -OutLog (New-LogPath "address-service\target\smoke-full-address.out.log") `
        -ErrLog (New-LogPath "address-service\target\smoke-full-address.err.log")
    Start-ServiceProcess `
        -ArgumentList @("-f", "task-service\pom.xml", "spring-boot:run") `
        -OutLog (New-LogPath "task-service\target\smoke-full-task.out.log") `
        -ErrLog (New-LogPath "task-service\target\smoke-full-task.err.log")
    Start-ServiceProcess `
        -ArgumentList @("-f", "meal-service\pom.xml", "spring-boot:run") `
        -OutLog (New-LogPath "meal-service\target\smoke-full-meal.out.log") `
        -ErrLog (New-LogPath "meal-service\target\smoke-full-meal.err.log")
    Start-ServiceProcess `
        -ArgumentList @("spring-boot:run") `
        -OutLog (New-LogPath "target\smoke-full-monolith.out.log") `
        -ErrLog (New-LogPath "target\smoke-full-monolith.err.log")
    Start-ServiceProcess `
        -ArgumentList @("-f", "gateway\pom.xml", "spring-boot:run") `
        -OutLog (New-LogPath "gateway\target\smoke-full-gateway.out.log") `
        -ErrLog (New-LogPath "gateway\target\smoke-full-gateway.err.log")

    Wait-HttpOk -Name "auth-service" -Url "http://localhost:8085/health" -TimeoutSeconds $StartupTimeoutSeconds
    Wait-HttpOk -Name "family-service" -Url "http://localhost:8084/health" -TimeoutSeconds $StartupTimeoutSeconds
    Wait-HttpOk -Name "address-service" -Url "http://localhost:8083/health" -TimeoutSeconds $StartupTimeoutSeconds
    Wait-HttpOk -Name "task-service" -Url "http://localhost:8081/health" -TimeoutSeconds $StartupTimeoutSeconds
    Wait-HttpOk -Name "meal-service" -Url "http://localhost:8086/health" -TimeoutSeconds $StartupTimeoutSeconds
    Wait-HttpOk -Name "monolith/BFF" -Url "http://localhost:8080/health" -TimeoutSeconds $StartupTimeoutSeconds
    Wait-HttpOk -Name "gateway" -Url "http://localhost:8082/gateway/health" -TimeoutSeconds $StartupTimeoutSeconds

    $username = "smoke-full-" + [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    $password = "SmokePass123!"
    $email = "$username@example.test"

    $registerBody = @{
        username = $username
        password = $password
        email = $email
    } | ConvertTo-Json
    $registerResponse = Invoke-RetryJsonPost `
        -Url "http://localhost:8082/auth/register" `
        -Body $registerBody `
        -ExpectedTarget "auth-service" `
        -IsReady { param($json) $json.result -eq $true -and $json.id } `
        -TimeoutSeconds $StartupTimeoutSeconds
    $register = $registerResponse.Content | ConvertFrom-Json

    $loginBody = @{
        username = $username
        password = $password
    } | ConvertTo-Json
    $loginResponse = Invoke-RetryJsonPost `
        -Url "http://localhost:8082/auth/login" `
        -Body $loginBody `
        -ExpectedTarget "auth-service" `
        -IsReady { param($json) $json.result -eq $true -and $json.accessToken -and $json.refreshToken } `
        -TimeoutSeconds $StartupTimeoutSeconds
    $login = $loginResponse.Content | ConvertFrom-Json

    $familyBody = @{
        name = "Smoke family"
        description = "full-stack smoke"
    } | ConvertTo-Json
    $familyResponse = Invoke-RetryJsonPost `
        -Url "http://localhost:8082/family/" `
        -AccessToken $login.accessToken `
        -Body $familyBody `
        -ExpectedTarget "family-service" `
        -IsReady { param($json) $json.result -eq $true -and $json.createResp.familyId } `
        -TimeoutSeconds $StartupTimeoutSeconds
    $family = $familyResponse.Content | ConvertFrom-Json
    $familyId = $family.createResp.familyId
    $adminMemberId = $family.createResp.adminMemberId

    $categoryBody = @{
        familyId = $familyId
        name = "smoke-category-$username"
    } | ConvertTo-Json
    $categoryResponse = Invoke-RetryJsonPost `
        -Url "http://localhost:8082/category/" `
        -AccessToken $login.accessToken `
        -Body $categoryBody `
        -ExpectedTarget "address-service" `
        -IsReady { param($json) $json.result -eq $true -and $json.createResp.categoryId } `
        -TimeoutSeconds $StartupTimeoutSeconds
    $category = $categoryResponse.Content | ConvertFrom-Json
    $categoryId = $category.createResp.categoryId

    $addressBody = @{
        familyId = $familyId
        categoryId = $categoryId
        name = "Smoke address"
        country = "RU"
        city = "Moscow"
        streetType = "STREET"
        street = "Tverskaya"
        house = "1"
        apartment = "1"
        comment = "full-stack smoke"
    } | ConvertTo-Json
    $addressResponse = Invoke-RetryJsonPost `
        -Url "http://localhost:8082/address/" `
        -AccessToken $login.accessToken `
        -Body $addressBody `
        -ExpectedTarget "address-service" `
        -IsReady { param($json) $json.result -eq $true -and $json.createResp.addressId } `
        -TimeoutSeconds $StartupTimeoutSeconds
    $address = $addressResponse.Content | ConvertFrom-Json
    $addressId = $address.createResp.addressId

    $taskBody = @{
        familyId = $familyId
        name = "Smoke task"
        assigneeId = $adminMemberId
        addressId = $addressId
        type = "OTHER"
        description = "full-stack smoke"
    } | ConvertTo-Json
    $taskResponse = Invoke-RetryJsonPost `
        -Url "http://localhost:8082/task/" `
        -AccessToken $login.accessToken `
        -Body $taskBody `
        -ExpectedTarget "task-service" `
        -IsReady { param($json) $json.result -eq $true -and $json.createResp.taskId } `
        -TimeoutSeconds $StartupTimeoutSeconds
    $task = $taskResponse.Content | ConvertFrom-Json

    $ingredientsResponse = Invoke-WebRequest `
        -Uri "http://localhost:8082/me/families/$familyId/food/ingredients" `
        -Headers @{ Authorization = "Bearer $($login.accessToken)" } `
        -UseBasicParsing
    if ($ingredientsResponse.Headers["X-FamilyHub-Gateway-Target"] -ne "meal-service") {
        throw "Food ingredients request was not routed by gateway to meal-service"
    }
    $ingredients = $ingredientsResponse.Content | ConvertFrom-Json
    if ($ingredients.result -ne $true -or $ingredients.categories.Count -lt 1 -or $ingredients.items.Count -lt 1) {
        throw "Unexpected food ingredients response: $($ingredientsResponse.Content)"
    }

    $recipeBody = @{
        familyId = $familyId
        name = "Smoke omelet"
        category = "Завтрак"
        description = "full-stack smoke"
        calories = "320"
        proteins = "22"
        fats = "24"
        carbs = "4"
        ingredients = @(
            @{
                ingredientId = 85
                name = $null
                categoryId = 3
                amount = "2"
                unit = "шт"
            }
        )
    } | ConvertTo-Json -Depth 5
    $recipeResponse = Invoke-RetryJsonPost `
        -Url "http://localhost:8082/food/recipe/" `
        -AccessToken $login.accessToken `
        -Body $recipeBody `
        -ExpectedTarget "meal-service" `
        -IsReady { param($json) $json.result -eq $true -and $json.createResp.recipeId -and $json.recipe.id } `
        -TimeoutSeconds $StartupTimeoutSeconds
    $recipe = $recipeResponse.Content | ConvertFrom-Json
    $recipeId = $recipe.createResp.recipeId

    $mealPlanDate = (Get-Date).Date.AddDays(1).ToString("yyyy-MM-dd")
    $mealPlanBody = @{
        familyId = $familyId
        date = $mealPlanDate
        slot = "BREAKFAST"
        note = "full-stack smoke"
        recipeIds = @($recipeId)
    } | ConvertTo-Json
    $mealPlanResponse = Invoke-RetryJsonPost `
        -Url "http://localhost:8082/food/meal-plan/batch" `
        -AccessToken $login.accessToken `
        -Body $mealPlanBody `
        -ExpectedTarget "meal-service" `
        -IsReady { param($json) $json.result -eq $true -and $json.createResp.groupId -and $json.items.Count -ge 1 } `
        -TimeoutSeconds $StartupTimeoutSeconds
    $mealPlan = $mealPlanResponse.Content | ConvertFrom-Json

    $bootstrapResponse = Invoke-WebRequest `
        -Uri "http://localhost:8082/me/bootstrap" `
        -Headers @{ Authorization = "Bearer $($login.accessToken)" } `
        -UseBasicParsing
    if ($bootstrapResponse.Headers["X-FamilyHub-Gateway-Target"] -ne "monolith") {
        throw "Bootstrap request was not routed by gateway to monolith"
    }
    $bootstrap = $bootstrapResponse.Content | ConvertFrom-Json
    if ($bootstrap.userId -ne $register.id -or -not $bootstrap.families -or $bootstrap.families.Count -lt 1) {
        throw "Unexpected bootstrap response: $($bootstrapResponse.Content)"
    }

    $overviewResponse = Invoke-WebRequest `
        -Uri "http://localhost:8082/me/families/$familyId/overview" `
        -Headers @{ Authorization = "Bearer $($login.accessToken)" } `
        -UseBasicParsing
    if ($overviewResponse.Headers["X-FamilyHub-Gateway-Target"] -ne "monolith") {
        throw "Overview request was not routed by gateway to monolith"
    }
    $overview = $overviewResponse.Content | ConvertFrom-Json
    if ($overview.result -ne $true `
            -or $overview.family.familyId -ne $familyId `
            -or $overview.categories.Count -lt 1 `
            -or $overview.addresses.Count -lt 1 `
            -or $overview.tasks.Count -lt 1) {
        throw "Unexpected overview response: $($overviewResponse.Content)"
    }

    Write-Host "Smoke passed: gateway-only auth, family, category, address, task, food, /me/bootstrap, and /me/families/{familyId}/overview flow are working. familyId=$familyId addressId=$addressId taskId=$($task.createResp.taskId) recipeId=$recipeId mealPlanId=$($mealPlan.items[0].id)"
} finally {
    foreach ($process in $processes) {
        if ($process -and -not $process.HasExited) {
            Stop-Process -Id $process.Id -Force
        }
    }
    Pop-Location
}
