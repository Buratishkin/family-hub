param(
    [int]$StartupTimeoutSeconds = 180
)

$ErrorActionPreference = "Stop"

$fullStackSmoke = Join-Path $PSScriptRoot "smoke-full-stack.ps1"
& $fullStackSmoke -StartupTimeoutSeconds $StartupTimeoutSeconds
if ($LASTEXITCODE) {
    exit $LASTEXITCODE
}
