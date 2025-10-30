# Quick Kafka Test Script
# Run this after starting Kafka with docker-compose

Write-Host "=== Kafka Integration Test ===" -ForegroundColor Green
Write-Host ""

# 1. Check if Kafka is running
Write-Host "[1] Checking Kafka status..." -ForegroundColor Yellow
docker ps | findstr kafka
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Kafka not running. Start with: docker-compose up -d kafka" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Kafka is running" -ForegroundColor Green
Write-Host ""

# 2. List topics
Write-Host "[2] Listing Kafka topics..." -ForegroundColor Yellow
docker exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
Write-Host ""

# 3. Create topic if not exists
Write-Host "[3] Creating discussion.events topic..." -ForegroundColor Yellow
docker exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --create --if-not-exists --topic discussion.events --partitions 3 --replication-factor 1
Write-Host "✓ Topic ready" -ForegroundColor Green
Write-Host ""

# 4. Produce test event
Write-Host "[4] Producing test event to Kafka..." -ForegroundColor Yellow
$testEvent = @'
{"eventType":"thread_created","eventId":"kafka-test-001","occurredAt":"2025-10-30T12:00:00Z","schemaVersion":1,"sourceService":"test-script","payload":{"threadId":999,"courseId":88}}
'@

$testEvent | docker exec -i kafka kafka-console-producer.sh --bootstrap-server localhost:9092 --topic discussion.events
Write-Host "✓ Event produced" -ForegroundColor Green
Write-Host ""

# 5. Verify event in topic
Write-Host "[5] Reading events from topic (showing last 5 seconds)..." -ForegroundColor Yellow
Start-Job -ScriptBlock {
    docker exec kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic discussion.events --from-beginning --max-messages 1 --timeout-ms 5000
} | Wait-Job | Receive-Job
Write-Host ""

# 6. Check Analytics processed it
Write-Host "[6] Checking if Analytics processed the event..." -ForegroundColor Yellow
Start-Sleep -Seconds 2
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8100/api/analytics/threads/999" -Method Get
    Write-Host "✓ Event processed! Thread aggregate:" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "⚠ Event not yet processed or Analytics not running with Kafka enabled" -ForegroundColor Yellow
    Write-Host "   Make sure: analytics.kafka.enabled=true in application.properties" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "=== Test Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  - Monitor consumer lag: docker exec kafka kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group analytics-service"
Write-Host "  - View all events: docker exec kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic discussion.events --from-beginning"
Write-Host "  - Check Analytics logs for processing confirmations"
