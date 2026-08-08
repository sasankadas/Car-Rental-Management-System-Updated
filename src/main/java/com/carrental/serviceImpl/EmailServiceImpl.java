package com.carrental.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.carrental.service.EmailService;

import jakarta.mail.internet.MimeMessage;
@Service
public class EmailServiceImpl implements EmailService{
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Value("${mail.from}")
	private String fromEmail;

	 @Override
	 public void sendVerificationMail(String to, String link, String name) {

	     try {
	         MimeMessage message = mailSender.createMimeMessage();

	         MimeMessageHelper helper =
	                 new MimeMessageHelper(message, true, "UTF-8");

	         helper.setFrom(fromEmail);
	         helper.setTo(to);
	         helper.setSubject("Verify Your Email - DriveEasy");

	         helper.setText(
	                 sendVerificationMailTemplate(name, link),
	                 true
	         );

	         mailSender.send(message);

	         System.out.println("✅ Verification email sent to: " + to);

	     } catch (Exception e) {

	         System.err.println("❌ EMAIL SENDING FAILED");
	         System.err.println("To: " + to);
	         System.err.println("From: " + fromEmail);

	         e.printStackTrace();

	         throw new RuntimeException("Failed to send verification email", e);
	     }
	 }

	@Override
	public String sendVerificationMailTemplate(String name, String link) {

	    return "<!DOCTYPE html>" +
	            "<html>" +
	            "<head>" +
	            "  <meta charset='UTF-8'>" +
	            "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
	            "  <title>Email Verification</title>" +
	            "</head>" +

	            "<body style='margin:0;padding:0;background-color:#f4f6f8;font-family:Arial,sans-serif;'>" +

	            "  <table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f6f8;padding:20px 0;'>" +
	            "    <tr>" +
	            "      <td align='center'>" +

	            "        <table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:10px;overflow:hidden;box-shadow:0 5px 15px rgba(0,0,0,0.1);'>" +

	            // Header
	            "          <tr>" +
	            "            <td style='background:#0d6efd;color:white;padding:20px;text-align:center;'>" +
	            "              <h2 style='margin:0;'>DriveEasy</h2>" +
	            "              <p style='margin:5px 0 0;'>Car Rental Service</p>" +
	            "            </td>" +
	            "          </tr>" +

	            // Body
	            "          <tr>" +
	            "            <td style='padding:30px;'>" +

	            "              <h3 style='margin-top:0;'>Hello, " + name + " 👋</h3>" +

	            "              <p style='color:#555;line-height:1.6;'>" +
	            "                Thank you for registering with <b>DriveEasy</b>. " +
	            "                Please confirm your email address to activate your account." +
	            "              </p>" +

	            "              <div style='text-align:center;margin:30px 0;'>" +
	            "                <a href='" + link + "' " +
	            "                   style='background:#0d6efd;color:#ffffff;padding:12px 25px;" +
	            "                   text-decoration:none;border-radius:6px;font-weight:bold;display:inline-block;'>" +
	            "                   Verify Email" +
	            "                </a>" +
	            "              </div>" +

	            "              <p style='color:#777;font-size:14px;'>" +
	            "                If the button above doesn’t work, copy and paste the link below into your browser:" +
	            "              </p>" +

	            "              <p style='word-break:break-all;color:#0d6efd;font-size:13px;'>" +
	            link +
	            "              </p>" +

	            "              <p style='color:#999;font-size:13px;margin-top:20px;'>" +
	            "                This link will expire in 15 minutes for security reasons." +
	            "              </p>" +

	            "            </td>" +
	            "          </tr>" +

	            // Footer
	            "          <tr>" +
	            "            <td style='background:#f1f1f1;padding:15px;text-align:center;font-size:12px;color:#888;'>" +
	            "              © 2026 DriveEasy. All rights reserved.<br>" +
	            "              Need help? Contact support@driveeasy.com" +
	            "            </td>" +
	            "          </tr>" +

	            "        </table>" +

	            "      </td>" +
	            "    </tr>" +
	            "  </table>" +

	            "</body>" +
	            "</html>";
	}

	@Override
	public void sendOtpMail(String to, String otp) {

	    try {

	        MimeMessage message = mailSender.createMimeMessage();

	        MimeMessageHelper helper =
	                new MimeMessageHelper(message, true, "UTF-8");

	        helper.setFrom(fromEmail);
	        helper.setTo(to);
	        helper.setSubject("Password Reset OTP");

	        helper.setText(
	                sendOtpMailTemplate(otp),
	                true
	        );

	        mailSender.send(message);

	        System.out.println("✅ OTP email sent to: " + to);

	    } catch (Exception e) {

	        System.err.println("❌ OTP EMAIL FAILED");
	        e.printStackTrace();

	        throw new RuntimeException("Failed to send OTP email", e);
	    }
	}

	@Override
	public String sendOtpMailTemplate(String otp) {
		   return "<!DOCTYPE html>" +
		            "<html>" +
		            "<body style='font-family: Arial; background:#f4f6f8; padding:20px;'>" +

		            "<div style='max-width:500px; margin:auto; background:white; padding:30px; border-radius:10px; box-shadow:0 5px 15px rgba(0,0,0,0.1);'>" +

		            "<h2 style='text-align:center; color:#333;'>Password Reset</h2>" +

		            "<p>Hello,</p>" +
		            "<p>You requested to reset your password.</p>" +

		            "<p style='text-align:center; font-size:22px; font-weight:bold; color:#2c7be5; letter-spacing:2px;'>" +
		            otp +
		            "</p>" +

		            "<p style='text-align:center;'>This OTP is valid for <b>5 minutes</b>.</p>" +

		            "<p>If you did not request this, please ignore this email.</p>" +

		            "<hr>" +
		            "<p style='font-size:12px; text-align:center; color:gray;'>Car Rental System</p>" +

		            "</div>" +
		            "</body>" +
		            "</html>";
		
	}

}
