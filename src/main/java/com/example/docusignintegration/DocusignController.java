package com.example.docusignintegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/docusign")
public class DocusignController {
    @Autowired
    private DocusignService docusignService;

    @PostMapping("/send")
    public void sendEnvelope(@RequestBody SendEnvelopeRequest request){
        docusignService.sendEnvelope(request);
    }
}
