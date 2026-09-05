package org.com.model

fun Room.toCreateRoomRequest(): CreateRoomRequest {
    return CreateRoomRequest(
        title = this.title ?: "",
        description = this.description ?: "",
        price = this.price,
        latitude = this.latitude,
        longitude = this.longitude,
        address = this.address ?: "",
        propertyType = this.propertyType ?: "",
        contactPhone = this.contactPhone ?: "",
        contactEmail = this.contactEmail ?: "",
        ownerName = this.ownerName ?: "",
        amenities = this.amenities,
        roomsCount = this.roomsCount,
        bathroomsCount = this.bathroomsCount,
        area = this.area,
        rules = this.rules,
        postedBy = this.postedBy ?: 0L,
        hasVideo = this.hasVideo,
        hasContract = this.hasContract,
        available = this.isAvailable,
        status = this.status
    )
}