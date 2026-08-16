param(
    [int]$StartupTimeoutSeconds = 120
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot "env.ps1")
$authServiceOutLog = Join-Path $repoRoot "auth-service\target\smoke-auth-service.out.log"
$authServiceErrLog = Join-Path $repoRoot "auth-service\target\smoke-auth-service.err.log"
$gatewayOutLog = Join-Path $repoRoot "gateway\target\smoke-auth-gateway.out.log"
$gatewayErrLog = Join-Path $repoRoot "gateway\target\smoke-auth-gateway.err.log"
$authServiceProcess = $null
$gatewayProcess = $null

function Wait-HttpOk {
    param(
        [string]$Url,
        [int]$TimeoutSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastError = $null
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -eq 200) {
                return
            }
        } catch {
            $lastError = $_
            Start-Sleep -Seconds 2
        }
    }

    throw "Timed out waiting for $Url. Last error: $lastError"
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

    docker compose up -d auth-db kafka

    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $authServiceOutLog) | Out-Null
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $gatewayOutLog) | Out-Null

    $authServiceProcess = Start-Process -FilePath ".\mvnw.cmd" `
        -ArgumentList "-f", "auth-service\pom.xml", "spring-boot:run" `
        -WorkingDirectory $repoRoot `
        -WindowStyle Hidden `
        -RedirectStandardOutput $authServiceOutLog `
        -RedirectStandardError $authServiceErrLog `
        -PassThru

    $gatewayProcess = Start-Process -FilePath ".\mvnw.cmd" `
        -ArgumentList "-f", "gateway\pom.xml", "spring-boot:run" `
        -WorkingDirectory $repoRoot `
        -WindowStyle Hidden `
        -RedirectStandardOutput $gatewayOutLog `
        -RedirectStandardError $gatewayErrLog `
        -PassThru

    Wait-HttpOk -Url "http://localhost:8085/health" -TimeoutSeconds $StartupTimeoutSeconds
    Wait-HttpOk -Url "http://localhost:8082/gateway/health" -TimeoutSeconds $StartupTimeoutSeconds

    $username = "smoke-auth-" + [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    $password = "SmokePass123!"
    $email = "$username@example.test"
    $registerBody = @{
        username = $username
        password = $password
        email = $email
    } | ConvertTo-Json

    $registerResponse = Invoke-WebRequest `
        -Method Post `
        -Uri "http://localhost:8082/auth/register" `
        -ContentType "application/json" `
        -Body $registerBody `
        -UseBasicParsing

    if ($registerResponse.Headers["X-FamilyHub-Gateway-Target"] -ne "auth-service") {
        throw "Register request was not routed by gateway to auth-service"
    }

    $register = $registerResponse.Content | ConvertFrom-Json
    if ($register.result -ne $true -or -not $register.userId) {
        throw "Unexpected auth-service register response: $($registerResponse.Content)"
    }

    $loginBody = @{
        username = $username
        password = $password
    } | ConvertTo-Json
    $loginResponse = Invoke-WebRequest `
        -Method Post `
        -Uri "http://localhost:8082/auth/login" `
        -ContentType "application/json" `
        -Body $loginBody `
        -UseBasicParsing

    if ($loginResponse.Headers["X-FamilyHub-Gateway-Target"] -ne "auth-service") {
        throw "Login request was not routed by gateway to auth-service"
    }

    $login = $loginResponse.Content | ConvertFrom-Json
    if ($login.result -ne $true -or -not $login.accessToken -or -not $login.refreshToken) {
        throw "Unexpected auth-service login response: $($loginResponse.Content)"
    }

    Write-Host "Smoke passed: auth-service health, gateway health, gateway /auth/register route, and gateway /auth/login route are working."
} finally {
    if ($gatewayProcess -and -not $gatewayProcess.HasExited) {
        Stop-Process -Id $gatewayProcess.Id -Force
    }
    if ($authServiceProcess -and -not $authServiceProcess.HasExited) {
        Stop-Process -Id $authServiceProcess.Id -Force
    }
    Pop-Location
}
