package base.boudicca.publisherhtml.controller

import base.boudicca.publisherhtml.service.PictureProxyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/")
class PictureProxyController @Autowired constructor(
    private val pictureProxyService: PictureProxyService
) {

    @GetMapping(
        value = ["/picture"],
        produces = [MediaType.IMAGE_JPEG_VALUE]
    )
    @ResponseBody
    fun getAbout(
        @RequestParam("url") url: String,
    ): ResponseEntity<ByteArray> {
        return ResponseEntity.of(pictureProxyService.getPicture(url))
    }

}





