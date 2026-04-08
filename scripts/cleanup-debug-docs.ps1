param(
    [string]$ApiBaseUrl = "http://localhost:8080",
    [int]$TopK = 50,
    [int]$MaxQueries = 4,
    [string]$IdPrefix = "debug-",
    [switch]$DryRun,
    [string[]]$QuerySeeds
)

$payload = @{
    topK = $TopK
    maxQueries = $MaxQueries
    idPrefix = $IdPrefix
    dryRun = [bool]$DryRun
}

if ($QuerySeeds -and $QuerySeeds.Count -gt 0) {
    $payload.querySeeds = $QuerySeeds
}

$uri = "$ApiBaseUrl/api/debug/dashvector/cleanup-debug-docs"

Write-Output "POST $uri"
Write-Output "Payload:"
$payload | ConvertTo-Json -Depth 6 | Write-Output

try {
    $resp = Invoke-RestMethod -Uri $uri -Method Post -ContentType "application/json" -Body ($payload | ConvertTo-Json -Depth 6)
    Write-Output "Response:"
    $resp | ConvertTo-Json -Depth 8 | Write-Output
} catch {
    Write-Error "Cleanup failed: $($_.Exception.Message)"
    if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
        Write-Output $_.ErrorDetails.Message
    }
    exit 1
}
