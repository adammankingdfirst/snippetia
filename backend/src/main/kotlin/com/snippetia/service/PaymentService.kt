package com.snippetia.service

import com.stripe.Stripe
import com.stripe.model.*
import com.stripe.param.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PaymentService {

    @Value("\${app.stripe.secret-key}")
    private lateinit var stripeSecretKey: String

    @Value("\${app.stripe.webhook-secret}")
    private lateinit var webhookSecret: String

    init {
        Stripe.apiKey = stripeSecretKey
    }

    fun processSubscriptionPayment(
        userId: Long,
        amount: BigDecimal,
        paymentMethodId: String,
        description: String
    ): PaymentResult {
        return try {
            // Create customer if not exists
            val customer = createOrGetCustomer(userId)

            // Attach payment method to customer
            val paymentMethod = PaymentMethod.retrieve(paymentMethodId)
            paymentMethod.attach(
                PaymentMethodAttachParams.builder()
                    .setCustomer(customer.id)
                    .build()
            )

            // Create subscription
            val subscription = Subscription.create(
                SubscriptionCreateParams.builder()
                    .setCustomer(customer.id)
                    .setDefaultPaymentMethod(paymentMethodId)
                    .addItem(
                        SubscriptionCreateParams.Item.builder()
                            .setPrice(createPrice(amount, description))
                            .build()
                    )
                    .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                    .setPaymentSettings(
                        SubscriptionCreateParams.PaymentSettings.builder()
                            .setSaveDefaultPaymentMethod(SubscriptionCreateParams.PaymentSettings.SaveDefaultPaymentMethod.ON_SUBSCRIPTION)
                            .build()
                    )
                    .setExpand(listOf("latest_invoice.payment_intent"))
                    .build()
            )

            PaymentResult(
                successful = subscription.status == "active" || subscription.status == "trialing",
                subscriptionId = subscription.id
            )
        } catch (e: Exception) {
            PaymentResult(
                successful = false,
                errorMessage = e.message
            )
        }
    }

    fun cancelSubscription(subscriptionId: String): Boolean {
        return try {
            val subscription = Subscription.retrieve(subscriptionId)
            subscription.cancel()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateSubscriptionAmount(subscriptionId: String, newAmount: BigDecimal): Boolean {
        return try {
            val subscription = Subscription.retrieve(subscriptionId)
            val subscriptionItem = subscription.items.data[0]

            SubscriptionItem.update(
                subscriptionItem.id,
                SubscriptionItemUpdateParams.builder()
                    .setPrice(createPrice(newAmount, "Updated subscription"))
                    .build()
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    fun processOneTimePayment(
        userId: Long,
        amount: BigDecimal,
        paymentMethodId: String,
        description: String
    ): PaymentResult {
        return try {
            val customer = createOrGetCustomer(userId)

            val paymentIntent = PaymentIntent.create(
                PaymentIntentCreateParams.builder()
                    .setAmount((amount * BigDecimal(100)).toLong()) // Convert to cents
                    .setCurrency("usd")
                    .setCustomer(customer.id)
                    .setPaymentMethod(paymentMethodId)
                    .setDescription(description)
                    .setConfirm(true)
                    .setReturnUrl("https://snippetia.com/payment/return")
                    .build()
            )

            PaymentResult(
                successful = paymentIntent.status == "succeeded",
                subscriptionId = paymentIntent.id
            )
        } catch (e: Exception) {
            PaymentResult(
                successful = false,
                errorMessage = e.message
            )
        }
    }

    fun createPaymentIntent(userId: Long, amount: BigDecimal, description: String): String {
        val customer = createOrGetCustomer(userId)

        val paymentIntent = PaymentIntent.create(
            PaymentIntentCreateParams.builder()
                .setAmount((amount * BigDecimal(100)).toLong())
                .setCurrency("usd")
                .setCustomer(customer.id)
                .setDescription(description)
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build()
        )

        return paymentIntent.clientSecret
    }

    fun handleWebhook(payload: String, signature: String): Boolean {
        return try {
            val event = Webhook.constructEvent(payload, signature, webhookSecret)
            
            when (event.type) {
                "invoice.payment_succeeded" -> {
                    val invoice = event.dataObjectDeserializer.`object`.get() as Invoice
                    handleSuccessfulPayment(invoice)
                }
                "invoice.payment_failed" -> {
                    val invoice = event.dataObjectDeserializer.`object`.get() as Invoice
                    handleFailedPayment(invoice)
                }
                "customer.subscription.deleted" -> {
                    val subscription = event.dataObjectDeserializer.`object`.get() as Subscription
                    handleSubscriptionCancelled(subscription)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun createOrGetCustomer(userId: Long): Customer {
        // In a real implementation, you'd store the Stripe customer ID in your database
        // For now, we'll create a new customer each time
        return Customer.create(
            CustomerCreateParams.builder()
                .setMetadata(mapOf("user_id" to userId.toString()))
                .build()
        )
    }

    private fun createPrice(amount: BigDecimal, description: String): String {
        val product = Product.create(
            ProductCreateParams.builder()
                .setName(description)
                .setType(ProductCreateParams.Type.SERVICE)
                .build()
        )

        val price = Price.create(
            PriceCreateParams.builder()
                .setProduct(product.id)
                .setUnitAmount((amount * BigDecimal(100)).toLong())
                .setCurrency("usd")
                .setRecurring(
                    PriceCreateParams.Recurring.builder()
                        .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
                        .build()
                )
                .build()
        )

        return price.id
    }

    private fun handleSuccessfulPayment(invoice: Invoice) {
        // Handle successful payment - update subscription status, send notifications, etc.
        val customerId = invoice.customer
        // Implementation depends on your business logic
    }

    private fun handleFailedPayment(invoice: Invoice) {
        // Handle failed payment - notify user, retry logic, etc.
        val customerId = invoice.customer
        // Implementation depends on your business logic
    }

    private fun handleSubscriptionCancelled(subscription: Subscription) {
        // Handle subscription cancellation
        val customerId = subscription.customer
        // Implementation depends on your business logic
    }
}