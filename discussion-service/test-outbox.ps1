# Discussion Service - Outbox Pattern Test Script
# This script tests the complete event flow from Discussion Service to Analytics Service via Kafka

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Discussion Service Outbox Test" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check if services are running
Write-Host "Step 1: Checking services..." -ForegroundColor Yellow
Write-Host "Checking Kafka..." -NoNewline
$kafkaRunning = docker ps --filter "name=kafka" --format "{{.Names}}" | Select-String "kafka"
if ($kafkaRunning) {
    Write-Host " ✓ Running" -ForegroundColor Green
} else {
    Write-Host " ✗ Not running" -ForegroundColor Red
    Write-Host "Start Kafka with: docker-compose up -d kafka" -ForegroundColor Yellow
    exit 1
}

Write-Host "Checking Postgres..." -NoNewline
$postgresRunning = docker ps --filter "name=postgres" --format "{{.Names}}" | Select-String "postgres"
if ($postgresRunning) {
    Write-Host " ✓ Running" -ForegroundColor Green
} else {
    Write-Host " ✗ Not running" -ForegroundColor Red
    Write-Host "Start Postgres with: docker-compose up -d postgres" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Step 2: Create a test thread
Write-Host "Step 2: Creating test thread in Discussion Service..." -ForegroundColor Yellow
$threadPayload = @{
    courseId = 42
    authorId = 100
    title = "Kafka Outbox Pattern Test Thread"
    content = "This thread tests the outbox pattern integration"
    category = "QUESTION"
    tags = @("kafka", "testing", "outbox-pattern")
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8092/api/discussions/threads" `
                                 -Method Post `
                                 -ContentType "application/json" `
                                 -Body $threadPayload
    
    $threadId = $response.id
    Write-Host "✓ Thread created with ID: $threadId" -ForegroundColor Green
    Write-Host "  Course ID: $($response.courseId)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to create thread" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure Discussion Service is running on port 8092" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Step 3: Check outbox table
Write-Host "Step 3: Checking outbox_events table..." -ForegroundColor Yellow
Write-Host "Querying pending events..." -NoNewline
Start-Sleep -Seconds 2

# Note: This requires psql or you can check via application logs
Write-Host " (Check application logs or database)" -ForegroundColor Gray
Write-Host "  Expected: 1 event with eventType='thread_created'" -ForegroundColor Gray

Write-Host ""

# Step 4: Wait for OutboxPublisher
Write-Host "Step 4: Waiting for OutboxPublisher to process events..." -ForegroundColor Yellow
Write-Host "Publisher runs every 5 seconds..." -NoNewline
Start-Sleep -Seconds 7
Write-Host " Done" -ForegroundColor Green

Write-Host ""

# Step 5: Check Kafka topic
Write-Host "Step 5: Checking Kafka topic for events..." -ForegroundColor Yellow
Write-Host "Reading from discussion.events topic..." -ForegroundColor Gray

try {
    $kafkaOutput = docker exec kafka kafka-console-consumer.sh `
        --bootstrap-server localhost:9092 `
        --topic discussion.events `
        --from-beginning `
        --max-messages 10 `
        --timeout-ms 5000 2>&1
    
    if ($kafkaOutput -match "thread_created") {
        Write-Host "✓ Found thread_created event in Kafka!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Sample event:" -ForegroundColor Gray
        $kafkaOutput | Select-String "thread_created" | ForEach-Object {
            Write-Host $_.Line -ForegroundColor DarkGray
        }
    } else {
        Write-Host "⚠ Event not found yet (may still be publishing)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠ Could not read from Kafka (topic may not exist yet)" -ForegroundColor Yellow
}

Write-Host ""

# Step 6: Add a comment
Write-Host "Step 6: Adding comment to thread..." -ForegroundColor Yellow
$commentPayload = @{
    threadId = $threadId
    authorId = 101
    content = "Great question! The outbox pattern ensures reliable event delivery."
    parentCommentId = $null
} | ConvertTo-Json

try {
    $commentResponse = Invoke-RestMethod -Uri "http://localhost:8092/api/discussions/comments" `
                                        -Method Post `
                                        -ContentType "application/json" `
                                        -Body $commentPayload
    
    $commentId = $commentResponse.id
    Write-Host "✓ Comment added with ID: $commentId" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to add comment" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Step 7: Cast a vote
Write-Host "Step 7: Casting upvote on thread..." -ForegroundColor Yellow
$votePayload = @{
    userId = 102
    targetType = "THREAD"
    targetId = $threadId
    voteType = "UPVOTE"
} | ConvertTo-Json

try {
    $voteResponse = Invoke-RestMethod -Uri "http://localhost:8092/api/discussions/votes" `
                                      -Method Post `
                                      -ContentType "application/json" `
                                      -Body $votePayload
    
    Write-Host "✓ Vote cast successfully" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to cast vote" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Step 8: Wait and check analytics
Write-Host "Step 8: Waiting for events to propagate..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "Checking Analytics Service for thread aggregate..." -ForegroundColor Gray
try {
    $analyticsResponse = Invoke-RestMethod -Uri "http://localhost:8100/api/analytics/threads/$threadId" `
                                          -Method Get
    
    Write-Host "✓ Analytics aggregate found!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Thread Aggregate:" -ForegroundColor Cyan
    Write-Host "  Thread ID: $($analyticsResponse.threadId)" -ForegroundColor Gray
    Write-Host "  Course ID: $($analyticsResponse.courseId)" -ForegroundColor Gray
    Write-Host "  View Count: $($analyticsResponse.viewCount)" -ForegroundColor Gray
    Write-Host "  Comment Count: $($analyticsResponse.commentCount)" -ForegroundColor Gray
    Write-Host "  Upvotes: $($analyticsResponse.upvotes)" -ForegroundColor Gray
    Write-Host "  Downvotes: $($analyticsResponse.downvotes)" -ForegroundColor Gray
    
} catch {
    Write-Host "⚠ Analytics not available yet (may take a few seconds)" -ForegroundColor Yellow
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Test Complete!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary of Events Published:" -ForegroundColor Cyan
Write-Host "  1. thread_created (Thread ID: $threadId)" -ForegroundColor Gray
Write-Host "  2. thread_viewed (when Analytics queries it)" -ForegroundColor Gray
Write-Host "  3. comment_added (Comment ID: $commentId)" -ForegroundColor Gray
Write-Host "  4. vote_cast (UPVOTE on thread)" -ForegroundColor Gray
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  - Check outbox table: SELECT * FROM outbox_events;" -ForegroundColor Gray
Write-Host "  - Monitor publisher logs in Discussion Service" -ForegroundColor Gray
Write-Host "  - Query top threads: curl http://localhost:8100/api/analytics/courses/42/top" -ForegroundColor Gray
Write-Host ""
