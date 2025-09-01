#!/bin/bash
echo "🚀 Starting User Service Database..."

docker-compose up postgres redis -d

echo "⏳ Waiting for database to be ready..."
sleep 5

# Check if PostgreSQL is ready
until docker exec user-service-postgres pg_isready -U postgres -d user_service_db; do
    echo "Waiting for PostgreSQL..."
    sleep 2
done

echo "✅ Database is ready!"
echo "📊 PostgreSQL: localhost:5432"
echo "🔴 Redis: localhost:6379"