# Supabase Setup Guide

## Problem Fixed
The app was freezing after scanning QR codes because the Supabase client was not properly configured.

## Solution

### 1. Get Your Supabase Credentials

1. Go to [Supabase Dashboard](https://app.supabase.com)
2. Select your project (or create a new one)
3. Go to **Settings** → **API**
4. Copy the following:
   - **Project URL** (something like `https://xxxxxxxxxxxxx.supabase.co`)
   - **anon/public key** (a long JWT token)

### 2. Configure gradle.properties

Open the file `gradle.properties` in the root of your project and add your credentials at the end:

```properties
# Supabase Configuration
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_KEY=your-anon-public-key-here
```

**Replace:**
- `https://your-project-id.supabase.co` with your actual Project URL
- `your-anon-public-key-here` with your actual anon/public key

**Note:** The file `gradle.properties.template` is provided as a reference. Your actual `gradle.properties` is in `.gitignore` to keep your credentials safe.

### 3. Rebuild the Project

After adding credentials:

1. Click **Build** → **Clean Project**
2. Click **Build** → **Rebuild Project**
3. Run the app

### 4. Verify Setup

When the app launches, you should see:
- ✅ A toast message: "Supabase connected successfully"
- ✅ In Logcat: "Supabase initialized successfully"

If credentials are missing or wrong, you'll see:
- ⚠️ A toast message: "Supabase not configured. Add credentials to local.properties"

## Changes Made

### 1. Fixed SupabaseManager.kt
- ✅ Fixed `isAvailable()` to properly check if client is initialized
- ✅ Added better error logging
- ✅ Added proper null checks

### 2. Fixed MainActivity.kt
- ✅ Made Supabase initialization asynchronous (prevents UI freezing)
- ✅ Added proper error handling with user feedback
- ✅ Added availability check in `processQr()` method
- ✅ All operations now run in coroutines (non-blocking)

### 3. Updated gradle.properties
- ✅ Added Supabase credentials (this file is now in .gitignore)
- ✅ Created gradle.properties.template as reference

## Database Schema Required

Make sure your Supabase project has these tables:

### carts table
```sql
CREATE TABLE carts (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  cartId TEXT UNIQUE NOT NULL,
  status TEXT DEFAULT 'available',
  created_at TIMESTAMP DEFAULT NOW()
);
```

### sessions table
```sql
CREATE TABLE sessions (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  sessionId TEXT UNIQUE NOT NULL,
  cartId TEXT REFERENCES carts(cartId),
  userId TEXT NOT NULL,
  status TEXT DEFAULT 'active',
  startedAt BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);
```

## Troubleshooting

### Issue: "Supabase client not available"
**Solution:** Make sure you've added valid credentials to `gradle.properties` and rebuilt the project (Build → Clean Project, then Build → Rebuild Project).

### Issue: "Service not ready. Please wait and try again."
**Solution:** Wait a few seconds after app launch for Supabase to initialize, or check your internet connection.

### Issue: App still freezes
**Solution:** Check Logcat for errors. Make sure you're using valid Supabase credentials.

## Testing

1. Launch the app - you should see a success toast
2. Navigate to the scanner screen
3. Scan a QR code (or use the demo scan button)
4. The app should remain responsive and show appropriate messages

## Security Note

⚠️ **IMPORTANT:** The `gradle.properties` file is now in `.gitignore`. Never commit your Supabase credentials to version control!

- ✅ `gradle.properties` - Contains your actual credentials (in .gitignore)
- ✅ `gradle.properties.template` - Safe template to commit to version control

For production builds, use environment variables or secure credential management.

