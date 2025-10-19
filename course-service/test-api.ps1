# Course Service API Test Script
Write-Host "`n=== COURSE SERVICE API TESTS ===" -ForegroundColor Cyan

# Test 1: Create a Course
Write-Host "`n1. Creating a new course..." -ForegroundColor Yellow
$courseBody = @{
    title = "Introduction to Spring Boot"
    description = "Learn Spring Boot from scratch with hands-on projects. Build REST APIs, microservices, and more!"
    instructorId = 1
    price = 49.99
    level = "BEGINNER"
    durationHours = 20
    language = "English"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/api/courses" `
        -Method Post `
        -Body $courseBody `
        -ContentType "application/json"
    
    Write-Host "✅ Course created successfully!" -ForegroundColor Green
    Write-Host "Course ID: $($response.id)" -ForegroundColor White
    Write-Host "Title: $($response.title)" -ForegroundColor White
    Write-Host "Status: $($response.status)" -ForegroundColor White
    $courseId = $response.id
} catch {
    Write-Host "❌ Error creating course: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Test 2: Get All Courses
Write-Host "`n2. Getting all courses..." -ForegroundColor Yellow
try {
    $courses = Invoke-RestMethod -Uri "http://localhost:8082/api/courses" -Method Get
    Write-Host "✅ Found $($courses.Count) course(s)" -ForegroundColor Green
    $courses | Format-Table id, title, level, price, status -AutoSize
} catch {
    Write-Host "❌ Error getting courses: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Get Course by ID
Write-Host "`n3. Getting course by ID ($courseId)..." -ForegroundColor Yellow
try {
    $course = Invoke-RestMethod -Uri "http://localhost:8082/api/courses/$courseId" -Method Get
    Write-Host "✅ Course retrieved successfully!" -ForegroundColor Green
    $course | Format-List
} catch {
    Write-Host "❌ Error getting course: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Update Course
Write-Host "`n4. Updating course..." -ForegroundColor Yellow
$updateBody = @{
    title = "Introduction to Spring Boot - Updated"
    description = "Learn Spring Boot from scratch with hands-on projects. Now with more examples!"
    instructorId = 1
    price = 59.99
    level = "BEGINNER"
    durationHours = 25
    language = "English"
} | ConvertTo-Json

try {
    $updated = Invoke-RestMethod -Uri "http://localhost:8082/api/courses/$courseId" `
        -Method Put `
        -Body $updateBody `
        -ContentType "application/json"
    
    Write-Host "✅ Course updated successfully!" -ForegroundColor Green
    Write-Host "New Title: $($updated.title)" -ForegroundColor White
    Write-Host "New Price: $($updated.price)" -ForegroundColor White
} catch {
    Write-Host "❌ Error updating course: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Publish Course
Write-Host "`n5. Publishing course..." -ForegroundColor Yellow
try {
    $published = Invoke-RestMethod -Uri "http://localhost:8082/api/courses/$courseId/publish" -Method Put
    Write-Host "✅ Course published successfully!" -ForegroundColor Green
    Write-Host "Status: $($published.status)" -ForegroundColor White
    Write-Host "Published At: $($published.publishedAt)" -ForegroundColor White
} catch {
    Write-Host "❌ Error publishing course: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Get Published Courses
Write-Host "`n6. Getting all published courses..." -ForegroundColor Yellow
try {
    $publishedCourses = Invoke-RestMethod -Uri "http://localhost:8082/api/courses/published" -Method Get
    Write-Host "✅ Found $($publishedCourses.Count) published course(s)" -ForegroundColor Green
    $publishedCourses | Format-Table id, title, status, publishedAt -AutoSize
} catch {
    Write-Host "❌ Error getting published courses: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 7: Get Courses by Instructor
Write-Host "`n7. Getting courses by instructor (ID: 1)..." -ForegroundColor Yellow
try {
    $instructorCourses = Invoke-RestMethod -Uri "http://localhost:8082/api/courses/instructor/1" -Method Get
    Write-Host "✅ Found $($instructorCourses.Count) course(s) for instructor" -ForegroundColor Green
    $instructorCourses | Format-Table id, title, level, price -AutoSize
} catch {
    Write-Host "❌ Error getting instructor courses: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== ALL TESTS COMPLETED ===" -ForegroundColor Cyan
Write-Host "`nNote: Delete test skipped to preserve data. To delete, run:" -ForegroundColor Gray
Write-Host "Invoke-RestMethod -Uri 'http://localhost:8082/api/courses/$courseId' -Method Delete" -ForegroundColor Gray
