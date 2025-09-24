# Email Service Debugging Guide - OBS Banking System

## Current Status ✅
- Email service has been **fully enhanced** with comprehensive logging and error handling
- Gmail SMTP configuration is **properly set up** in `application.yml`
- Test endpoint `/api/super-admin/test-email` has been added for debugging
- All email methods now use **SLF4J logger** instead of System.err.println

## Email Configuration Details

### SMTP Settings (application.yml)
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ananthagunde@gmail.com
    password: iryt qoeb dwls wabf  # Gmail App Password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
    default-encoding: UTF-8
    test-connection: false
```

## Enhanced Email Methods

### 1. Welcome Email (Registration)
- **Enhanced messaging** with login URL and username
- **Proper error handling** with detailed logging
- **From address** properly set using mailProperties.getUsername()

### 2. Account Locked Email
- **Security focus** with clear instructions
- **Contact information** for support
- **Professional formatting**

### 3. Password Reset Email
- **Temporary password delivery**
- **Security warnings** about immediate password change
- **Clear login instructions**

### 4. OTP Email
- **Secure OTP delivery** with validity period
- **Anti-phishing warnings** (don't share OTP)
- **Security-focused messaging**

### 5. Transaction Alert Email
- **Real-time transaction notifications**
- **Security contact information**
- **Professional transaction details formatting**

## Testing Email Service

### Method 1: Test Endpoint (Recommended)
```bash
POST http://localhost:8085/api/super-admin/test-email
Authorization: Bearer <super_admin_jwt_token>
Content-Type: application/json

{
    "email": "test@example.com"
}
```

**Response on Success:**
```json
{
    "success": true,
    "message": "Test email sent successfully to test@example.com",
    "note": "Please check your email (including spam folder) and server logs for details"
}
```

**Response on Error:**
```json
{
    "success": false,
    "error": "Failed to send test email: Connection timed out",
    "details": "MailConnectException"
}
```

### Method 2: Registration Testing
1. Create a new user account through `/api/auth/register`
2. Check application logs for email sending attempts
3. Check email inbox (including spam folder)

### Method 3: Log Monitoring
Watch the application logs during email operations:
```bash
# Check logs for email operations
tail -f logs/spring.log | grep -i "email\|mail"
```

## Debugging Checklist

### ✅ Configuration Verification
- [x] Gmail SMTP settings correct (smtp.gmail.com:587)
- [x] STARTTLS enabled and required
- [x] Authentication enabled
- [x] Valid Gmail account and app password
- [x] From address properly set

### ✅ Code Enhancements
- [x] SLF4J logger implemented for all email methods
- [x] Proper exception handling with stack traces
- [x] Detailed error messages for different failure scenarios
- [x] Success logging for sent emails
- [x] Enhanced email content with professional formatting

### 🔧 Common Issues & Solutions

#### Issue 1: "Authentication failed"
- **Cause:** Invalid Gmail app password
- **Solution:** Generate new Gmail App Password:
  1. Go to Google Account Settings
  2. Security → 2-Step Verification → App Passwords
  3. Generate new password for "Mail"
  4. Update `MAIL_PASSWORD` in application.yml

#### Issue 2: "Connection timeout"
- **Cause:** Network/firewall blocking SMTP
- **Solution:** Check firewall settings, try different network

#### Issue 3: "Mail server connection failed"
- **Cause:** Incorrect SMTP settings
- **Solution:** Verify host:port (smtp.gmail.com:587)

#### Issue 4: Emails go to spam
- **Cause:** Gmail's spam filtering
- **Solution:** 
  - Check spam folder
  - Add sender to contacts
  - Use proper SPF/DKIM (production setup)

## Log Examples

### Successful Email Send:
```
2024-09-24 10:05:00 INFO  EmailService - Attempting to send welcome email to: user@example.com
2024-09-24 10:05:02 INFO  EmailService - Welcome email sent successfully to: user@example.com
```

### Failed Email Send:
```
2024-09-24 10:05:00 INFO  EmailService - Attempting to send welcome email to: user@example.com
2024-09-24 10:05:05 ERROR EmailService - Failed to send welcome email to user@example.com: Connection timed out
org.springframework.mail.MailConnectException: Mail server connection failed...
```

## Next Steps

1. **Test the email service** using the new test endpoint
2. **Monitor logs** during email operations for detailed debugging
3. **Check spam folders** for delivered emails
4. **Verify Gmail app password** if authentication fails
5. **Contact system administrator** if network issues persist

## Production Recommendations

For production deployment:
- Use environment variables for email credentials
- Implement email queue system for reliability
- Add retry mechanisms for failed sends
- Set up proper SPF/DKIM records
- Monitor email delivery rates
- Implement email templates for consistency

---
*Email service debugging completed - comprehensive logging and testing capabilities now available*