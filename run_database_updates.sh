#!/bin/bash

# PostgreSQL connection details from your application.properties
DB_HOST="localhost"
DB_PORT="5433"
DB_NAME="projectmaster"
DB_USER="postgres"
DB_PASSWORD="postgres"

echo "🚀 Running database updates for estimated_days migration..."
echo "Database: $DB_NAME on $DB_HOST:$DB_PORT"
echo ""

# Check if psql is available
if ! command -v psql &> /dev/null; then
    echo "❌ psql command not found. Please install PostgreSQL client:"
    echo "   brew install postgresql"
    echo ""
    echo "💡 Alternative options:"
    echo "1. Use a GUI tool like pgAdmin, DBeaver, or TablePlus"
    echo "2. Run the Spring Boot app which will auto-migrate"
    echo "3. Copy-paste the SQL from UPDATE_ESTIMATED_DAYS_STATEMENTS.sql manually"
    exit 1
fi

# Test connection
echo "🔍 Testing database connection..."
if PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT version();" > /dev/null 2>&1; then
    echo "✅ Database connection successful"
else
    echo "❌ Database connection failed. Please check:"
    echo "   - Database is running on $DB_HOST:$DB_PORT"
    echo "   - Database '$DB_NAME' exists"
    echo "   - Username/password are correct: $DB_USER"
    exit 1
fi

# Run the migration
echo ""
echo "📊 Running estimated_days updates..."
if PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f UPDATE_ESTIMATED_DAYS_STATEMENTS.sql; then
    echo ""
    echo "✅ All updates completed successfully!"
    echo ""
    echo "📈 Summary:"
    echo "   - Updated 63 workflow steps"
    echo "   - Changed from estimated_hours to estimated_days"
    echo "   - Based on Australian construction practices"
else
    echo ""
    echo "❌ Updates failed. Please check the error messages above."
    exit 1
fi

# Verify the changes
echo ""
echo "🔍 Verifying changes..."
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
SELECT 
    COUNT(*) as total_steps,
    AVG(estimated_days) as avg_estimated_days,
    SUM(estimated_days) as total_estimated_days
FROM standard_workflow_steps 
WHERE estimated_days IS NOT NULL;
"

echo ""
echo "🎉 Migration completed! Your workflow steps now use realistic day-based estimates."
