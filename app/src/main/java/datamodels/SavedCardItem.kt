package datamodels

data class SavedCardItem(
    var cardNumber: String = "CARD_NUMBER",
    var cardHolderName: String = "CARD_HOLDER_NAME",
    var cardExpiryDate: String = "CARD_EXPIRY_DATE",
    var isSelected: Boolean = false
)