# ImageComparison

## Описание
`ImageComparison` - это проект на Java, который сравнивает два изображения на пиксельном уровне и генерирует HTML-страницу, отображающую оба изображения и количество областей, в которых есть различия.

## Требования
- Java 17 или выше
- Maven

## Запуск и работа программы
Поместите ваши изображение в ```src/main/resources/images```
<br />
Укажите эталон как изображение А и сравниваемое как B
```
BufferedImage img1 = ImageIO.read(new File("src/main/resources/images/imageA.jpg"));
BufferedImage img2 = ImageIO.read(new File("src/main/resources/images/imageB.jpg"));
```

Запустите программу.
Результат сравнения с указанием областей, будет доступен в виде html файла ```comparison.html``` в корне проекта

Откройте его любым доступным браузером.

## Настройки 
Выбрать точность и радиус совместных пикселей можно через указание целых чисел в переменных
```
MIN_DIFF_PIXELS
NEIGHBOR_RADIUS
```

