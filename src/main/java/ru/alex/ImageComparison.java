package ru.alex;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.util.*;

public class ImageComparison {

    private static final int[] DX = {-1, 0, 1, 0};
    private static final int[] DY = {0, 1, 0, -1};
    public static void main(String[] args) {
        try {
            BufferedImage img1 = ImageIO.read(new File("src/main/resources/images/imageA.jpg"));
            BufferedImage img2 = ImageIO.read(new File("src/main/resources/images/imageB.jpg"));

            List<int[]> diffPixels = findDifferingPixels(img1, img2);

            // Исправление здесь: получаем результат работы метода markDifferingPixels
            MarkResult result = markDifferingPixels(img2, diffPixels);

            // И теперь мы передаем изображение и количество различий отдельно

            generateHtml(img1, result.markedImage, result.totalDifferences);

        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public static List<int[]> findDifferingPixels(BufferedImage img1, BufferedImage img2) {
        List<int[]> differingPixels = new ArrayList<>();

        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            throw new IllegalArgumentException("Images must have the same dimensions.");
        }

        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    differingPixels.add(new int[]{x, y});
                }
            }
        }

        return differingPixels;
    }
    public static class MarkResult {
        public final BufferedImage markedImage;
        public final int totalDifferences;

        public MarkResult(BufferedImage markedImage, int totalDifferences) {
            this.markedImage = markedImage;
            this.totalDifferences = totalDifferences;
        }
    }

    public static MarkResult markDifferingPixels(BufferedImage img, List<int[]> diffPixels) {
        int[][] labels = new int[img.getWidth()][img.getHeight()];
        int nextLabel = 1;

        // Сначала помечаем все различающиеся пиксели
        for (int[] pixel : diffPixels) {
            if (labels[pixel[0]][pixel[1]] == 0) {
                floodFill(pixel[0], pixel[1], nextLabel++, labels, img, diffPixels);
            }
        }

        // Создаём пустое изображение для рисования рамок
        BufferedImage outlineImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2dOutline = outlineImage.createGraphics();

        // Устанавливаем настройки пера для рамки
        g2dOutline.setColor(Color.RED);
        g2dOutline.setStroke(new BasicStroke(1));

        // Проходимся по меткам и рисуем рамки
        for (int label = 1; label < nextLabel; label++) {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            // Находим минимальные и максимальные координаты для различающихся пикселей для текущей метки
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    if (labels[x][y] == label) {
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                    }
                }
            }

            // Рисуем рамку на outlineImage
            g2dOutline.drawRect(minX, minY, maxX - minX + 1, maxY - minY + 1);
        }

        // Заканчиваем рисовать на outlineImage и освобождаем ресурсы
        g2dOutline.dispose();

        // Накладываем изображение с рамками на исходное
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(outlineImage, 0, 0, null);
        g2d.dispose();
        int totalDifferences = nextLabel - 1;
        return new MarkResult(img, totalDifferences);
    }

    private static void floodFill(int x, int y, int label, int[][] labels, BufferedImage img, List<int[]> diffPixels) {

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{x, y});
        labels[x][y] = label;

        while (!queue.isEmpty()) {
            int[] pixel = queue.poll();

            for (int i = 0; i < 4; i++) {
                int nx = pixel[0] + DX[i];
                int ny = pixel[1] + DY[i];

                // Проверяем, что следующий пиксель находится внутри изображения и в списке различающихся пикселей
                if (nx >= 0 && nx < img.getWidth() && ny >= 0 && ny < img.getHeight() && labels[nx][ny] == 0 && isDiffPixel(nx, ny, diffPixels)) {
                    queue.add(new int[]{nx, ny});
                    labels[nx][ny] = label;
                }
            }
        }
    }

    private static boolean isDiffPixel(int x, int y, List<int[]> diffPixels) {
        for (int[] diffPixel : diffPixels) {
            if (diffPixel[0] == x && diffPixel[1] == y) {
                return true;
            }
        }
        return false;
    }
    public static void generateHtml(BufferedImage img1, BufferedImage img2WithDifferences, int totalDifferences)
            throws IOException {


        try (PrintWriter writer = new PrintWriter("comparison.html", StandardCharsets.UTF_8)) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<meta charset=\"UTF-8\">"); // Мета-тег для кодировки UTF-8
            writer.println("<title>Image Comparison</title>");
            writer.println("<style>");
            writer.println(".container { display: flex; flex-direction: column; align-items: center; }"); // Стили для общего контейнера
            writer.println(".images { display: flex; justify-content: center; }"); // Стили для контейнера изображений
            writer.println(".images img { margin: 10px; border: 1px solid black; max-width: 45%; }"); // Стили для изображений
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");

            // Общий контейнер
            writer.println("<div class='container'>");

            // Контейнер для изображений
            writer.println("<div class='images'>");
            writer.println("<img src='data:image/jpeg;base64," + encodeToString(img1, "jpg") + "' alt='Image 1'>");
            writer.println("<img src='data:image/jpeg;base64," + encodeToString(img2WithDifferences, "jpg") + "' alt='Image 2'>");
            writer.println("</div>");

            // Добавляем сообщение, если изображения идентичны
            // Если изображения идентичны, сообщаем об этом
            if (totalDifferences == 0) {
                writer.println("<h1>Изображения идентичны!</h1>");
            } else {
                // В противном случае выводим количество областей, в которых есть различия
                writer.println("<p>Number of different areas: " + totalDifferences + ".</p>");
            }
            writer.println("</div>");

            writer.println("</body>");
            writer.println("</html>");
            writer.close();
        }
    }




    public static String encodeToString(BufferedImage image, String type) throws IOException {
        String imageString;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();
            imageString = Base64.getEncoder().encodeToString(imageBytes);
        }
        return imageString;
    }
}
