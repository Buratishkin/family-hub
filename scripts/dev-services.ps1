param(
    [ValidateSet("start", "stop", "restart", "status")]
    [string]$Action = "restart",
    [int]$StartupTimeoutSeconds = 180,
    [switch]$SkipInfrastructure
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot "env.ps1")
Import-FamilyHubEnv -RepoRoot $repoRoot
$defaultFirebaseCredentialsPath = Join-Path $env:USERPROFILE ".familyhub\firebase-admin.json"

if (-not $env:FIREBASE_CREDENTIALS_PATH -and (Test-Path $defaultFirebaseCredentialsPath)) {
    $env:FIREBASE_CREDENTIALS_PATH = $defaultFirebaseCredentialsPath
}

$services = @(
    @{
        Name = "auth-service"
        Port = 8085
        HealthUrl = "http://localhost:8085/health"
        ArgumentList = @("-f", "auth-service\pom.xml", "spring-boot:run")
        OutLog = "auth-service\target\dev-services.out.log"
        ErrLog = "auth-service\target\dev-services.err.log"
    },
    @{
        Name = "family-service"
        Port = 8084
        HealthUrl = "http://localhost:8084/health"
        ArgumentList = @("-f", "family-service\pom.xml", "spring-boot:run")
        OutLog = "family-service\target\dev-services.out.log"
        ErrLog = "family-service\target\dev-services.err.log"
    },
    @{
        Name = "address-service"
        Port = 8083
        HealthUrl = "http://localhost:8083/health"
        ArgumentList = @("-f", "address-service\pom.xml", "spring-boot:run")
        OutLog = "address-service\target\dev-services.out.log"
        ErrLog = "address-service\target\dev-services.err.log"
    },
    @{
        Name = "task-service"
        Port = 8081
        HealthUrl = "http://localhost:8081/health"
        ArgumentList = @("-f", "task-service\pom.xml", "spring-boot:run")
        OutLog = "task-service\target\dev-services.out.log"
        ErrLog = "task-service\target\dev-services.err.log"
    },
    @{
        Name = "meal-service"
        Port = 8086
        HealthUrl = "http://localhost:8086/health"
        ArgumentList = @("-f", "meal-service\pom.xml", "spring-boot:run")
        OutLog = "meal-service\target\dev-services.out.log"
        ErrLog = "meal-service\target\dev-services.err.log"
    },
    @{
        Name = "notification-service"
        Port = 8087
        HealthUrl = "http://localhost:8087/health"
        ArgumentList = @("-f", "notification-service\pom.xml", "spring-boot:run")
        OutLog = "notification-service\target\dev-services.out.log"
        ErrLog = "notification-service\target\dev-services.err.log"
    },
    @{
        Name = "monolith-bff"
        Port = 8080
        HealthUrl = "http://localhost:8080/health"
        ArgumentList = @("spring-boot:run")
        OutLog = "target\dev-services.out.log"
        ErrLog = "target\dev-services.err.log"
    },
    @{
        Name = "gateway"
        Port = 8082
        HealthUrl = "http://localhost:8082/gateway/health"
        ArgumentList = @("-f", "gateway\pom.xml", "spring-boot:run")
        OutLog = "gateway\target\dev-services.out.log"
        ErrLog = "gateway\target\dev-services.err.log"
    }
)

function Test-DockerAvailable {
    if ([System.Environment]::OSVersion.Platform -eq "Win32NT" `
            -and -not (Test-Path "\\.\pipe\dockerDesktopLinuxEngine") `
            -and -not (Test-Path "\\.\pipe\docker_engine")) {
        throw "Docker daemon pipe was not found. Start Docker Desktop and rerun this script."
    }
}

function Get-ListeningProcessIds {
    param([int]$Port)

    @(Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique)
}

function Stop-ServiceByPort {
    param([hashtable]$Service)

    $processIds = Get-ListeningProcessIds -Port $Service.Port
    if ($processIds.Count -eq 0) {
        Write-Host "OK: $($Service.Name) is not listening on port $($Service.Port)"
        return
    }

    foreach ($processId in $processIds) {
        Write-Host "Stopping $($Service.Name) on port $($Service.Port), pid=$processId"
        Stop-Process -Id $processId -Force
    }
}

function Wait-PortFree {
    param(
        [hashtable]$Service,
        [int]$TimeoutSeconds = 30
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if ((Get-ListeningProcessIds -Port $Service.Port).Count -eq 0) {
            return
        }
        Start-Sleep -Seconds 1
    }

    throw "Timed out waiting for $($Service.Name) port $($Service.Port) to become free."
}

function Start-ServiceProcess {
    param([hashtable]$Service)

    if ((Get-ListeningProcessIds -Port $Service.Port).Count -gt 0) {
        Write-Host "OK: $($Service.Name) is already listening on port $($Service.Port)"
        return
    }

    $outLog = Join-Path $repoRoot $Service.OutLog
    $errLog = Join-Path $repoRoot $Service.ErrLog
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $outLog) | Out-Null

    Start-Process -FilePath ".\mvnw.cmd" `
        -ArgumentList $Service.ArgumentList `
        -WorkingDirectory $repoRoot `
        -WindowStyle Hidden `
        -RedirectStandardOutput $outLog `
        -RedirectStandardError $errLog | Out-Null

    Write-Host "Started $($Service.Name); logs: $($Service.OutLog), $($Service.ErrLog)"
}

function Wait-HttpOk {
    param(
        [hashtable]$Service,
        [int]$TimeoutSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastError = $null
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri $Service.HealthUrl -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -eq 200) {
                Write-Host "OK: $($Service.Name) is healthy at $($Service.HealthUrl)"
                return
            }
        } catch {
            $lastError = $_
            Start-Sleep -Seconds 2
        }
    }

    throw "Timed out waiting for $($Service.Name) at $($Service.HealthUrl). Last error: $lastError"
}

function Show-ServiceStatus {
    foreach ($service in $services) {
        $processIds = Get-ListeningProcessIds -Port $service.Port
        if ($processIds.Count -gt 0) {
            Write-Host "RUNNING: $($service.Name) port=$($service.Port) pid=$($processIds -join ',')"
        } else {
            Write-Host "STOPPED: $($service.Name) port=$($service.Port)"
        }
    }
}

try {
    Push-Location $repoRoot

    if ($Action -eq "status") {
        Show-ServiceStatus
        return
    }

    if ($Action -eq "stop" -or $Action -eq "restart") {
        foreach ($service in $services) {
            Stop-ServiceByPort -Service $service
        }
        foreach ($service in $services) {
            Wait-PortFree -Service $service
        }
    }

    if ($Action -eq "start" -or $Action -eq "restart") {
        Assert-FamilyHubEnv

        if (-not $SkipInfrastructure) {
            Test-DockerAvailable
            docker compose up -d db auth-db family-db address-db task-db meal-db notification-db kafka
            Write-Host "OK: Docker infrastructure is up"
        }

        foreach ($service in $services) {
            Start-ServiceProcess -Service $service
        }
        foreach ($service in $services) {
            Wait-HttpOk -Service $service -TimeoutSeconds $StartupTimeoutSeconds
        }
    }
} finally {
    Pop-Location
}
