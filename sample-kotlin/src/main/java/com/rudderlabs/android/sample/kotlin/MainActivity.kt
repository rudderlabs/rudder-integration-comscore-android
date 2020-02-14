package com.rudderlabs.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderMessageBuilder
import com.rudderstack.android.sdk.core.ecomm.*
import com.rudderstack.android.sdk.core.ecomm.events.*

class MainActivity : AppCompatActivity() {
    private var count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ECommerce Product
        val productA = ECommerceProduct.Builder()
            .withProductId("some_product_id_a")
            .withSku("some_product_sku_a")
            .withCurrency("USD")
            .withPrice(2.99f)
            .withName("Some Product Name A")
            .withQuantity(1f)
            .build()

        val productB = ECommerceProduct.Builder()
            .withProductId("some_product_id_b")
            .withSku("some_product_sku_b")
            .withCurrency("USD")
            .withPrice(3.99f)
            .withName("Some Product Name B")
            .withQuantity(1f)
            .build()

        val productC = ECommerceProduct.Builder()
            .withProductId("some_product_id_c")
            .withSku("some_product_sku_c")
            .withCurrency("USD")
            .withPrice(4.99f)
            .withName("Some Product Name C")
            .withQuantity(1f)
            .build()

        // ECommerce WishList
        val wishList = ECommerceWishList.Builder()
            .withWishListId("some_wish_list_id")
            .withWishListName("Some Wish List Name")
            .build()

        // ECommerce Cart
        val cart = ECommerceCart.Builder()
            .withCartId("some_cart_id")
            .withProduct(productA)
            .withProduct(productB)
            .withProduct(productC)
            .build()

        // ECommerce Order
        val order = ECommerceOrder.Builder()
            .withOrderId("some_order_id")
            .withAffiliation("some_order_affiliation")
            .withCoupon("some_coupon")
            .withCurrency("USD")
            .withDiscount(1.49f)
            .withProducts(productA, productB, productC)
            .withRevenue(10.99f)
            .withShippingCost(2.49f)
            .withTax(1.49f)
            .withTotal(12.99f)
            .withValue(10.49f)
            .build()

        // ECommerce Checkout event
        val checkout = ECommerceCheckout.Builder()
            .withCheckoutId("some_checkout_id")
            .withOrderId("some_order_id")
            .withPaymentMethod("Visa")
            .withShippingMethod("FedEx")
            .withStep(1)
            .build()

        MainApplication.rudderClient.identify("some_user_id")

        val productAddedToCartEvent = ProductAddedToCartEvent()
            .withCartId("some_cart_id")
            .withProduct(productA)
        MainApplication.rudderClient.track(
            RudderMessageBuilder()
                .setEventName(productAddedToCartEvent.event())
                .setProperty(productAddedToCartEvent.properties())
                .build()
        )

        val productAddedToWishListEvent = ProductAddedToWishListEvent()
            .withWishList(wishList)
            .withProduct(productA)
        MainApplication.rudderClient.track(
            RudderMessageBuilder()
                .setProperty(productAddedToWishListEvent.properties())
                .setEventName(productAddedToWishListEvent.event())
        )

        val cartViewedEvent = CartViewedEvent().withCart(cart)
        MainApplication.rudderClient.track(cartViewedEvent.event(), cartViewedEvent.properties())

//        MainApplication.rudderClient.reset()

        val checkoutStartedEvent = CheckoutStartedEvent().withOrder(order)
        MainApplication.rudderClient.track(
            checkoutStartedEvent.event(),
            checkoutStartedEvent.properties()
        )

        val paymentInfoEnteredEvent = PaymentInfoEnteredEvent()
            .withCheckout(checkout)
        MainApplication.rudderClient.track(
            paymentInfoEnteredEvent.event(),
            paymentInfoEnteredEvent.properties()
        )

        val orderCompletedEvent = OrderCompletedEvent().withOrder(order)
        MainApplication.rudderClient.track(
            orderCompletedEvent.event(),
            orderCompletedEvent.properties()
        )

        val spendCreditEvent = OrderCompletedEvent().withOrder(order)
        MainApplication.rudderClient.track(
            "Spend Credits",
            spendCreditEvent.properties()
        )

        val productSearchedEvent = ProductSearchedEvent().withQuery("blue hot pants")
        MainApplication.rudderClient.track(
            productSearchedEvent.event(),
            productSearchedEvent.properties()
        )

        val productViewedEvent = ProductViewedEvent()
            .withProduct(productA);
        MainApplication.rudderClient.track(
            productViewedEvent.event(),
            productViewedEvent.properties()
        )

        val productListViewedEvent = ProductListViewedEvent()
            .withProducts(productA, productB, productC)
        MainApplication.rudderClient.track(
            productListViewedEvent.event(),
            productListViewedEvent.properties()
        )

        val productReviewedEvent = ProductReviewedEvent()
            .withProduct(productA)
            .withRating(2.0)
            .withReviewBody("Some Review Body")
            .withReviewId("some_review_id")
        MainApplication.rudderClient.track(
            productReviewedEvent.event(),
            productReviewedEvent.properties()
        )

        val productSharedEvent = ProductSharedEvent()
            .withProduct(productA)
            .withRecipient("friend@gmail.com")
            .withShareMessage("Some Share Message")
            .withSocialChannel("Gmail")
        MainApplication.rudderClient.track(
            productSharedEvent.event(),
            productSharedEvent.properties()
        )
    }
}
