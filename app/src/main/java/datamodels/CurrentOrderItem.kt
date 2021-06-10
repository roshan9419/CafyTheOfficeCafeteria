package datamodels

data class CurrentOrderItem(
    var orderID: String = "ORDER_ID",
    var takeAwayTime: String = "TAKE_AWAY_TIME",
    var paymentStatus: String = "PAYMENT_STATUS",
    var orderItemNames: String = "ORDER_ITEM_NAMES",
    var orderItemQuantities: String = "ORDER_ITEM_QUANTITIES",
    var totalItemPrice: String = "TOTAL_ITEM_PRICE",
    var tax: String = "TAX",
    var subTotal: String = "SUB_TOTAL",
    var id: Int = 0
)