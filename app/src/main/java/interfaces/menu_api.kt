package interfaces

import datamodels.MenuItem

interface MenuApi {
    fun onFetchSuccessListener(list: ArrayList<MenuItem>)
}