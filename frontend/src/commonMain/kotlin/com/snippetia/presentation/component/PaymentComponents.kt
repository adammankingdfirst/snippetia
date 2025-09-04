package com.snippetia.presentation.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Advanced Payment Components with stunning animations and cross-platform support
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onPaymentComplete: (PaymentResult) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) }
    var paymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var currentStep by remember { mutableStateOf(PaymentStep.PLAN_SELECTION) }
    var isProcessing by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        // Animated background particles
        ParticleSystemBackground(
            modifier = Modifier.fillMaxSize(),
            particleCount = 30,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Indicator
            PaymentProgressIndicator(
                currentStep = currentStep,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Step Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() with
                    slideOutHorizontally { -it } + fadeOut()
                },
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    PaymentStep.PLAN_SELECTION -> {
                        PlanSelectionStep(
                            selectedPlan = selectedPlan,
                            onPlanSelected = { selectedPlan = it },
                            onNext = { 
                                if (selectedPlan != null) {
                                    currentStep = PaymentStep.PAYMENT_METHOD
                                }
                            }
                        )
                    }
                    PaymentStep.PAYMENT_METHOD -> {
                        PaymentMethodStep(
                            selectedMethod = paymentMethod,
                            onMethodSelected = { paymentMethod = it },
                            onNext = {
                                if (paymentMethod != null) {
                                    currentStep = PaymentStep.PAYMENT_DETAILS
                                }
                            },
                            onBack = { currentStep = PaymentStep.PLAN_SELECTION }
                        )
                    }
                    PaymentStep.PAYMENT_DETAILS -> {
                        PaymentDetailsStep(
                            plan = selectedPlan!!,
                            method = paymentMethod!!,
                            isProcessing = isProcessing,
                            onPayment = { details ->
                                isProcessing = true
                                // Process payment
                            },
                            onBack = { currentStep = PaymentStep.PAYMENT_METHOD }
                        )
                    }
                }
            }
        }
        
        // Processing Overlay
        if (isProcessing) {
            PaymentProcessingOverlay(
                onComplete = { result ->
                    isProcessing = false
                    onPaymentComplete(result)
                }
            )
        }
    }
}

@Composable
private fun PaymentProgressIndicator(
    currentStep: PaymentStep,
    modifier: Modifier = Modifier
) {
    val steps = PaymentStep.values()
    val progress by animateFloatAsState(
        targetValue = (currentStep.ordinal + 1f) / steps.size,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    Column(modifier = modifier) {
        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(2.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Step Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEach { step ->
                StepIndicator(
                    step = step,
                    isActive = step.ordinal <= currentStep.ordinal,
                    isCurrent = step == currentStep
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(
    step: PaymentStep,
    isActive: Boolean,
    isCurrent: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isCurrent) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .scale(scale)
                .background(
                    color = if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .border(
                    width = if (isCurrent) 2.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = step.icon,
                contentDescription = step.title,
                tint = if (isActive) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = step.title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlanSelectionStep(
    selectedPlan: SubscriptionPlan?,
    onPlanSelected: (SubscriptionPlan) -> Unit,
    onNext: () -> Unit
) {
    val plans = remember { getSubscriptionPlans() }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Your Plan",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Unlock premium features and boost your productivity",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(plans) { plan ->
                PlanCard(
                    plan = plan,
                    isSelected = selectedPlan == plan,
                    onSelect = { onPlanSelected(plan) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AnimatedVisibility(
            visible = selectedPlan != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        label = "borderColor"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box {
            // Holographic effect for premium plans
            if (plan.isPremium) {
                HolographicEffect {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
            
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = plan.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = plan.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (plan.isPopular) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text(
                                text = "POPULAR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Price
                Row(
                    verticalAlignment = Alignment.Baseline
                ) {
                    Text(
                        text = "$${plan.price}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "/${plan.billingPeriod}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (plan.originalPrice > plan.price) {
                    Text(
                        text = "Was $${plan.originalPrice}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Features
                plan.features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodStep(
    selectedMethod: PaymentMethod?,
    onMethodSelected: (PaymentMethod) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val paymentMethods = remember { getPaymentMethods() }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Payment Method",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Choose how you'd like to pay",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(paymentMethods) { method ->
                PaymentMethodCard(
                    method = method,
                    isSelected = selectedMethod == method,
                    onSelect = { onMethodSelected(method) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("Back")
            }
            
            AnimatedVisibility(
                visible = selectedMethod != null,
                modifier = Modifier.weight(1f)
            ) {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Continue")
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        label = "borderColor"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = method.icon,
                    contentDescription = method.name,
                    tint = method.color,
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = method.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = method.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
        }
    }
}

@Composable
private fun PaymentDetailsStep(
    plan: SubscriptionPlan,
    method: PaymentMethod,
    isProcessing: Boolean,
    onPayment: (PaymentDetails) -> Unit,
    onBack: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var billingAddress by remember { mutableStateOf("") }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Payment Details",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Order Summary
        OrderSummaryCard(plan = plan)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Payment Form
        when (method.type) {
            PaymentMethodType.CREDIT_CARD -> {
                CreditCardForm(
                    cardNumber = cardNumber,
                    onCardNumberChange = { cardNumber = it },
                    expiryDate = expiryDate,
                    onExpiryDateChange = { expiryDate = it },
                    cvv = cvv,
                    onCvvChange = { cvv = it },
                    cardholderName = cardholderName,
                    onCardholderNameChange = { cardholderName = it }
                )
            }
            PaymentMethodType.PAYPAL -> {
                PayPalForm()
            }
            PaymentMethodType.APPLE_PAY -> {
                ApplePayForm()
            }
            PaymentMethodType.GOOGLE_PAY -> {
                GooglePayForm()
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = !isProcessing
            ) {
                Text("Back")
            }
            
            Button(
                onClick = {
                    val details = PaymentDetails(
                        cardNumber = cardNumber,
                        expiryDate = expiryDate,
                        cvv = cvv,
                        cardholderName = cardholderName,
                        billingAddress = billingAddress
                    )
                    onPayment(details)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = !isProcessing && cardNumber.isNotBlank()
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Pay $${plan.price}")
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(plan: SubscriptionPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "$${plan.price}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (plan.discount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Discount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "-$${plan.discount}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${plan.price - plan.discount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CreditCardForm(
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    expiryDate: String,
    onExpiryDateChange: (String) -> Unit,
    cvv: String,
    onCvvChange: (String) -> Unit,
    cardholderName: String,
    onCardholderNameChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Credit Card Preview
        CreditCardPreview(
            cardNumber = cardNumber,
            expiryDate = expiryDate,
            cardholderName = cardholderName
        )
        
        // Card Number
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { value ->
                val formatted = formatCardNumber(value)
                if (formatted.length <= 19) { // 16 digits + 3 spaces
                    onCardNumberChange(formatted)
                }
            },
            label = { Text("Card Number") },
            placeholder = { Text("1234 5678 9012 3456") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = "Card Number"
                )
            }
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Expiry Date
            OutlinedTextField(
                value = expiryDate,
                onValueChange = { value ->
                    val formatted = formatExpiryDate(value)
                    if (formatted.length <= 5) { // MM/YY
                        onExpiryDateChange(formatted)
                    }
                },
                label = { Text("Expiry") },
                placeholder = { Text("MM/YY") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            
            // CVV
            OutlinedTextField(
                value = cvv,
                onValueChange = { value ->
                    if (value.length <= 4 && value.all { it.isDigit() }) {
                        onCvvChange(value)
                    }
                },
                label = { Text("CVV") },
                placeholder = { Text("123") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.weight(1f)
            )
        }
        
        // Cardholder Name
        OutlinedTextField(
            value = cardholderName,
            onValueChange = onCardholderNameChange,
            label = { Text("Cardholder Name") },
            placeholder = { Text("John Doe") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CreditCardPreview(
    cardNumber: String,
    expiryDate: String,
    cardholderName: String
) {
    val rotationY by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(600),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotationY),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF667eea),
                                Color(0xFF764ba2)
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Card Type Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = getCardType(cardNumber),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Card Number
                    Text(
                        text = if (cardNumber.isBlank()) "•••• •••• •••• ••••" else cardNumber,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    
                    // Cardholder and Expiry
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "CARDHOLDER",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = if (cardholderName.isBlank()) "YOUR NAME" else cardholderName.uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Column {
                            Text(
                                text = "EXPIRES",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = if (expiryDate.isBlank()) "MM/YY" else expiryDate,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentProcessingOverlay(
    onComplete: (PaymentResult) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var processingStep by remember { mutableStateOf(0) }
    val processingSteps = listOf(
        "Validating payment details...",
        "Processing payment...",
        "Confirming transaction...",
        "Activating subscription..."
    )
    
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            processingSteps.forEachIndexed { index, _ ->
                processingStep = index
                delay(1500)
            }
            
            // Simulate payment completion
            onComplete(
                PaymentResult(
                    success = true,
                    transactionId = "txn_${System.currentTimeMillis()}",
                    message = "Payment successful!"
                )
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Processing Icon
                val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )
                
                Icon(
                    Icons.Default.Sync,
                    contentDescription = "Processing",
                    modifier = Modifier
                        .size(64.dp)
                        .rotate(rotation),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Processing Payment",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AnimatedContent(
                    targetState = processingStep,
                    transitionSpec = {
                        slideInVertically { it } + fadeIn() with
                        slideOutVertically { -it } + fadeOut()
                    },
                    label = "processing_step"
                ) { step ->
                    Text(
                        text = processingSteps.getOrNull(step) ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                LinearProgressIndicator(
                    progress = (processingStep + 1f) / processingSteps.size,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Additional payment method forms
@Composable
private fun PayPalForm() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0070BA).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = "PayPal",
                tint = Color(0xFF0070BA),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "You'll be redirected to PayPal to complete your payment securely.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ApplePayForm() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Smartphone,
                contentDescription = "Apple Pay",
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Use Touch ID or Face ID to pay with Apple Pay.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GooglePayForm() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4285F4).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Payment,
                contentDescription = "Google Pay",
                tint = Color(0xFF4285F4),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Pay quickly and securely with Google Pay.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Data classes and enums
enum class PaymentStep(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PLAN_SELECTION("Plan", Icons.Default.Subscriptions),
    PAYMENT_METHOD("Method", Icons.Default.Payment),
    PAYMENT_DETAILS("Details", Icons.Default.CreditCard)
}

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val originalPrice: Double = price,
    val discount: Double = 0.0,
    val billingPeriod: String,
    val features: List<String>,
    val isPopular: Boolean = false,
    val isPremium: Boolean = false
)

data class PaymentMethod(
    val id: String,
    val name: String,
    val description: String,
    val type: PaymentMethodType,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

enum class PaymentMethodType {
    CREDIT_CARD, PAYPAL, APPLE_PAY, GOOGLE_PAY
}

data class PaymentDetails(
    val cardNumber: String,
    val expiryDate: String,
    val cvv: String,
    val cardholderName: String,
    val billingAddress: String
)

data class PaymentResult(
    val success: Boolean,
    val transactionId: String?,
    val message: String
)

// Helper functions
private fun getSubscriptionPlans(): List<SubscriptionPlan> {
    return listOf(
        SubscriptionPlan(
            id = "free",
            name = "Free",
            description = "Perfect for getting started",
            price = 0.0,
            billingPeriod = "forever",
            features = listOf(
                "5 public snippets",
                "Basic code editor",
                "Community support",
                "Basic analytics"
            )
        ),
        SubscriptionPlan(
            id = "pro",
            name = "Pro",
            description = "For serious developers",
            price = 9.99,
            originalPrice = 14.99,
            discount = 5.0,
            billingPeriod = "month",
            features = listOf(
                "Unlimited snippets",
                "Private repositories",
                "Advanced code editor",
                "AI assistance",
                "Priority support",
                "Advanced analytics",
                "Team collaboration"
            ),
            isPopular = true
        ),
        SubscriptionPlan(
            id = "enterprise",
            name = "Enterprise",
            description = "For large teams and organizations",
            price = 29.99,
            billingPeriod = "month",
            features = listOf(
                "Everything in Pro",
                "SSO integration",
                "Advanced security",
                "Custom branding",
                "Dedicated support",
                "SLA guarantee",
                "Advanced admin controls"
            ),
            isPremium = true
        )
    )
}

private fun getPaymentMethods(): List<PaymentMethod> {
    return listOf(
        PaymentMethod(
            id = "credit_card",
            name = "Credit Card",
            description = "Visa, Mastercard, American Express",
            type = PaymentMethodType.CREDIT_CARD,
            icon = Icons.Default.CreditCard,
            color = Color(0xFF1976D2)
        ),
        PaymentMethod(
            id = "paypal",
            name = "PayPal",
            description = "Pay with your PayPal account",
            type = PaymentMethodType.PAYPAL,
            icon = Icons.Default.AccountBalance,
            color = Color(0xFF0070BA)
        ),
        PaymentMethod(
            id = "apple_pay",
            name = "Apple Pay",
            description = "Touch ID or Face ID",
            type = PaymentMethodType.APPLE_PAY,
            icon = Icons.Default.Smartphone,
            color = Color.Black
        ),
        PaymentMethod(
            id = "google_pay",
            name = "Google Pay",
            description = "Quick and secure payments",
            type = PaymentMethodType.GOOGLE_PAY,
            icon = Icons.Default.Payment,
            color = Color(0xFF4285F4)
        )
    )
}

private fun formatCardNumber(input: String): String {
    val digits = input.filter { it.isDigit() }
    return digits.chunked(4).joinToString(" ")
}

private fun formatExpiryDate(input: String): String {
    val digits = input.filter { it.isDigit() }
    return when {
        digits.length <= 2 -> digits
        else -> "${digits.substring(0, 2)}/${digits.substring(2, minOf(4, digits.length))}"
    }
}

private fun getCardType(cardNumber: String): String {
    val digits = cardNumber.filter { it.isDigit() }
    return when {
        digits.startsWith("4") -> "VISA"
        digits.startsWith("5") -> "MASTERCARD"
        digits.startsWith("3") -> "AMEX"
        else -> "CARD"
    }
}