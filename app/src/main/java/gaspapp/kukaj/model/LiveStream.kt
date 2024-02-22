package gaspapp.kukaj.model

import org.jsoup.nodes.Element
import java.io.Serializable

data class LiveStream(
    var id: Long = 0,
    var title: String? = null,
    var shortDescription: String? = null,
    var description: String? = null,
    var backgroundImageUrl: String? = null,
    var cardImageUrl: String? = null,
    var detailUrl: String? = null,
    var videoUrl: String? = null,
    var isInMaintenance: Boolean = false
) : Serializable {

    override fun toString(): String {
        return "LiveStream{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", description='" + description + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", detailUrl='" + detailUrl + '\'' +
                ", backgroundImageUrl='" + backgroundImageUrl + '\'' +
                ", cardImageUrl='" + cardImageUrl + '\'' +
                '}'
    }

    companion object {
        internal const val serialVersionUID = 727566175075960653L

        fun fromHtmlElement(element: Element): LiveStream? {
            val detailUrl = element.select(".card-body > a").attr("href")
            val title = element.select(".card-body > a").attr("title")
            val shortDescription = element.select(".card-body > p").text()
            val thumbnailImageUrl = element.select("img").attr("src")
            val backgroundImageUrl = thumbnailImageUrl.split("/").dropLast(1).joinToString("/")
            if (detailUrl.equals("")) {
                return null
            }
            val id = detailUrl.split("/").last().split("-").first().toLong()
            return LiveStream(id, title, shortDescription, "", backgroundImageUrl, thumbnailImageUrl, detailUrl, "")
        }
    }
}