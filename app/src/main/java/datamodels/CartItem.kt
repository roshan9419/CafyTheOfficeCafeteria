package datamodels

data class CartItem(
    var imageUrl: String = "IMAGE_URL",
    var itemName: String = "ITEM NAME",
    var itemPrice: Float = 0.0F,
    var itemShortDesc: String = "ITEM DESC",
    var itemStars: Float = 5.0F,
    var quantity: Int = 0
)
