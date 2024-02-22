package gaspapp.kukaj.model

data class LiveStreamCategory(
    var category: CategoryModel,
    var liveStreamList: List<LiveStream>,
){ }