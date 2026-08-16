function Import-FamilyHubEnv {
    param(
        [Parameter(Mandatory = $true)]
        [string]$RepoRoot
    )

    $envPath = Join-Path $RepoRoot ".env"
    if (-not (Test-Path $envPath)) {
        return
    }

    Get-Content $envPath | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith("#")) {
            return
        }

        $parts = $line.Split("=", 2)
        if ($parts.Count -ne 2) {
            return
        }

        $name = $parts[0].Trim()
        $value = $parts[1].Trim()
        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }

        if ($name -and -not [Environment]::GetEnvironmentVariable($name, "Process")) {
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
        }
    }
}

function Assert-FamilyHubEnv {
    $required = @(
        "FAMILYHUB_POSTGRES_USER",
        "FAMILYHUB_POSTGRES_PASSWORD",
        "FAMILYHUB_JWT_SECRET",
        "FAMILYHUB_ENCRYPTOR_PASSWORD",
        "FAMILYHUB_ENCRYPTOR_SALT",
        "FAMILYHUB_INTERNAL_TOKEN"
    )

    $missing = $required | Where-Object {
        [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($_, "Process"))
    }

    if ($missing.Count -gt 0) {
        throw "Missing required FamilyHub environment variables: $($missing -join ', '). Create .env from .env.example or set them in the current shell."
    }
}
