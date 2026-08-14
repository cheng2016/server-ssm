param(
    [string]$Base = "http://localhost:8080",
    [string]$OpsToken = "dev-ops-token",
    [string]$UserName = "alice",
    [string]$Password = "secret1"
)

$ErrorActionPreference = "Stop"

function Show-Json([string]$Title, [string]$Body) {
    Write-Host "== $Title =="
    try {
        $Body | ConvertFrom-Json | ConvertTo-Json -Depth 8
    } catch {
        Write-Host $Body
    }
    Write-Host ""
}

$health = Invoke-RestMethod -Uri "$Base/actuator/health"
Show-Json "health" ($health | ConvertTo-Json -Depth 8)

try {
    $register = Invoke-RestMethod -Method Post -Uri "$Base/api/auth/register" -ContentType "application/json" -Body (@{
        username = $UserName
        password = $Password
        nickname = "Alice"
    } | ConvertTo-Json)
    Show-Json "register" ($register | ConvertTo-Json -Depth 8)
} catch {
    Write-Host "register skipped (user may already exist)"
    Write-Host ""
}

$login = Invoke-RestMethod -Method Post -Uri "$Base/api/auth/login" -ContentType "application/json" -Body (@{
    username = $UserName
    password = $Password
} | ConvertTo-Json)
Show-Json "login" ($login | ConvertTo-Json -Depth 8)

$online = Invoke-RestMethod -Uri "$Base/api/ops/online" -Headers @{ "X-Ops-Token" = $OpsToken }
Show-Json "online" ($online | ConvertTo-Json -Depth 8)

Write-Host "Swagger: $Base/swagger-ui.html"
Write-Host "Debug:   $Base/debug.html"
Write-Host "Health:  $Base/actuator/health"
