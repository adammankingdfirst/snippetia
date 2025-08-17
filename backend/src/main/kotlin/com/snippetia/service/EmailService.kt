package com.snippetia.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender
) {

    @Value("\${app.mail.from}")
    private lateinit var fromEmail: String

    @Value("\${app.frontend.url}")
    private lateinit var frontendUrl: String

    fun sendVerificationEmail(email: String, token: String) {
        val message = SimpleMailMessage().apply {
            setTo(email)
            setFrom(fromEmail)
            subject = "Verify your Snippetia account"
            text = """
                Welcome to Snippetia!
                
                Please click the link below to verify your email address:
                $frontendUrl/verify-email?token=$token
                
                If you didn't create an account with us, please ignore this email.
                
                Best regards,
                The Snippetia Team
            """.trimIndent()
        }
        
        mailSender.send(message)
    }

    fun sendPasswordResetEmail(email: String, token: String) {
        val message = SimpleMailMessage().apply {
            setTo(email)
            setFrom(fromEmail)
            subject = "Reset your Snippetia password"
            text = """
                Hello,
                
                You requested to reset your password for your Snippetia account.
                
                Please click the link below to reset your password:
                $frontendUrl/reset-password?token=$token
                
                This link will expire in 1 hour.
                
                If you didn't request a password reset, please ignore this email.
                
                Best regards,
                The Snippetia Team
            """.trimIndent()
        }
        
        mailSender.send(message)
    }
}