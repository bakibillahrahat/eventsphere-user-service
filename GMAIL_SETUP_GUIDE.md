# Gmail SMTP Authentication Setup Guide

## The Problem
Your application is failing to authenticate with Gmail SMTP because Gmail requires App Passwords when 2-factor authentication is enabled, or specific security settings need to be configured.

## Solutions (Try in Order)

### Option 1: Use App Password (Recommended)
If you have 2-factor authentication enabled on your Gmail account:

1. **Enable 2-Factor Authentication** (if not already enabled):
   - Go to [Google Account Security](https://myaccount.google.com/security)
   - Enable 2-Step Verification

2. **Generate App Password**:
   - Go to [App Passwords](https://myaccount.google.com/apppasswords)
   - Select "Mail" as the app
   - Select "Other" as the device and name it "EventSphere User Service"
   - Copy the 16-character password (e.g., `abcd efgh ijkl mnop`)

3. **Update your environment variables**:
   ```bash
   EMAIL_USERNAME=bakibillahrahat203@gmail.com
   EMAIL_PASSWORD=abcdefghijklmnop  # Use the App Password (remove spaces)
   ```

### Option 2: Enable Less Secure Apps (Not Recommended)
If you don't want to use 2FA:

1. Go to [Less Secure App Access](https://myaccount.google.com/lesssecureapps)
2. Turn ON "Allow less secure apps"
3. Use your regular Gmail password

### Option 3: OAuth2 (Most Secure - Advanced)
For production applications, consider implementing OAuth2 authentication.

## Configuration Changes Made

I've updated your configuration with the following improvements:

### application.yml
- Fixed mail configuration indentation (was under JPA instead of Spring)
- Updated to use Gmail SMTP server
- Added enhanced security properties

### MailConfig.java
- Added SSL trust configuration
- Specified TLS version 1.2
- Increased timeout values
- Reduced debug logging

## Testing Your Setup

After updating your EMAIL_PASSWORD with an App Password, restart your application and test email functionality.

## Troubleshooting

If you still get authentication errors:

1. **Double-check credentials**: Ensure EMAIL_USERNAME and EMAIL_PASSWORD are correct
2. **Check Gmail settings**: Make sure the account allows SMTP access
3. **Network issues**: Ensure your network allows outbound connections on port 587
4. **Account locked**: Check if Gmail has temporarily locked your account due to suspicious activity

## Environment Variables Required

Make sure these are set in your `.env` file or environment:

```
EMAIL_USERNAME=your-gmail@gmail.com
EMAIL_PASSWORD=your-app-password-or-regular-password
```

## Security Best Practices

1. Use App Passwords instead of regular passwords
2. Never commit credentials to version control
3. Use environment variables for sensitive configuration
4. Consider OAuth2 for production applications
5. Monitor email usage and set up alerts for unusual activity
