package com.example.signup;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfService {

    public byte[] generateCoursePdf(Course course) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Add title
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText(course.getTitle());
                contentStream.endText();

                // Add description
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 700);

                // Split description into lines
                String[] descriptionLines = course.getDescription().split("(?<=\\G.{80})");
                float leading = 15.0f;
                for (String line : descriptionLines) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -leading);
                }
                contentStream.endText();

                // Add image if exists
                if (course.getImageUrl() != null) {
                    try {
                        Path imagePath = Paths.get(course.getImageUrl().replace("/uploads/", "uploads/"));
                        PDImageXObject image = PDImageXObject.createFromFile(imagePath.toString(), document);

                        // Scale image to fit page width while maintaining aspect ratio
                        float scale = 400f / image.getWidth();
                        contentStream.drawImage(image, 50, 400,
                                image.getWidth() * scale, image.getHeight() * scale);
                    } catch (IOException e) {
                        // Handle image loading error
                        System.err.println("Could not load image: " + e.getMessage());
                    }
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}