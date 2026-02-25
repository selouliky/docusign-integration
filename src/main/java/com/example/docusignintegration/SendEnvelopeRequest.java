package com.example.docusignintegration;

import java.util.List;

public record SendEnvelopeRequest(
        String templateId,
        List<Signataire> signataires
) {}
