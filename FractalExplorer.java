import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

// Основной класс "фрактала"
public class FractalExplorer {
    private int displaySize;
    private JImageDisplay display;
    private FractalGenerator fractal;
    private Rectangle2D.Double range;
    private JComboBox comboBox;

    // Инициализация
    public FractalExplorer(int size){
        displaySize = size;
        // fractal = new Tricorn();
        // fractal = new BurningShip();
        fractal = new Mandelbrot();
        range = new Rectangle2D.Double();
        fractal.getInitialRange(range);
        display = new JImageDisplay(displaySize, displaySize);
    }

    // Создание окна приложения
    public void createAndShowGUI(){
        // Создания объекта окна
        JFrame frame = new JFrame("Фрактал Мандельброта");

        // Установка действия по умолчанию, при нажатии на кнопку закрытия приложения
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Установка BorderLayout для окна
        display.setLayout(new BorderLayout());

        // Добавление дисплея в окно прилодения
        frame.add(display, BorderLayout.CENTER);


        JPanel bottomButtonsPanel = new JPanel();

        // Создание кнопки сброса
        JButton resetButton = new JButton("Reset");
        // Создание уловителя события
        resetButton.addActionListener(new ButtonHandler());
        // Добавление кнопки в окно приложения
        bottomButtonsPanel.add(resetButton);

        JButton saveButton = new JButton("Save image");
        saveButton.addActionListener(new ButtonHandler());
        bottomButtonsPanel.add(saveButton);

        frame.add(bottomButtonsPanel, BorderLayout.SOUTH);

        MouseHandler click = new MouseHandler();
        display.addMouseListener(click);

        JPanel comboBoxPanel = new JPanel();

        JLabel comboBoxLabel = new JLabel("Fractal type:");

        String[] fractalTypes = {"Mandelbrot", "Tricorn", "BurningShip"};
        comboBox = new JComboBox(fractalTypes);
        ComboboxEventHandler choice = new ComboboxEventHandler();
        comboBox.addActionListener(choice);

        comboBoxPanel.add(comboBoxLabel);
        comboBoxPanel.add(comboBox);

        frame.add(comboBoxPanel, BorderLayout.NORTH);


        // Упаковка фрейма
        frame.pack();
        // Установка состояния окна в видимое
        frame.setVisible(true);
        // Запрет изменения размеров окна
        frame.setResizable(false);
    }

    // Отрисовка текущего фрактала
    private void drawFractal(){
        // Прохождение по каждой координате фрема
        for (int x = 0; x < displaySize; x++){
            for (int y = 0; y < displaySize; y++){

                // Определение мнимых координат
                double xCoord = fractal.getCoord(range.x, range.x + range.width, displaySize, x);
                double yCoord = fractal.getCoord(range.y,range.y + range.height, displaySize, y);

                // Подсчёт количества итераций
                int iteration = fractal.numIterations(xCoord, yCoord);

                // float hue = 0.6f + (float) iteration / 200f;

                // Выбор цвета в зависимости от количества итераций
                int rgbColor = iteration == -1 ? 0 : 0xFFFFFF / 2000 * (2000 - iteration); //Color.HSBtoRGB(hue, 1f, 1f);

                // Установка цвета для конкретного пикселя
                display.drawPixel(x, y, rgbColor);
            }
        }

        // Обновление фрейма
        display.repaint();
    }


    private class ComboboxEventHandler implements ActionListener {
        public void actionPerformed(ActionEvent e){
            String selected = Objects.requireNonNull(comboBox.getSelectedItem()).toString();
            switch (selected) {
                case "Mandelbrot" -> fractal = new Mandelbrot();
                case "Tricorn" -> fractal = new Tricorn();
                case "BurningShip" -> fractal = new BurningShip();
            }
            fractal.getInitialRange(range);
            drawFractal();
        }
    }

    // Определение класса, отвечающего за события программы
    private class ButtonHandler implements ActionListener {
        // Метод вызываемый при событии программы
        public void actionPerformed(ActionEvent e){
            String command = e.getActionCommand();
            // Проеврка на команду кнопки
            if (command.equals("Reset")) {
                // Сброс прямоугольника к значениям по умолчанию
                fractal.getInitialRange(range);
                // Отрисовка фрактала
                drawFractal();
            }

            if (command.equals("Save image")){
                JFileChooser chooser = new JFileChooser();
                FileFilter filter = new FileNameExtensionFilter("PNG Images", "png");
                chooser.setFileFilter(filter);
                chooser.setAcceptAllFileFilterUsed(false);

                int result = chooser.showSaveDialog(new JLabel(comboBox.getSelectedItem().toString()));
                if (result == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    try {
                        ImageIO.write(display.getImage(), "png", f);
                        // throw new Exception("invalid");
                    } catch (Exception err) {
                        JOptionPane.showMessageDialog(display, err, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    // Определение класса, отвечающего за события нажатия мыши
    private class MouseHandler extends MouseAdapter {
        // Метод вызываемый при нажатии мыши
        @Override
        public void mouseClicked(MouseEvent e){
            // получение координат клика
            int x = e.getX();
            int y = e.getY();

            // получение координат фрактала
            double xCoord = fractal.getCoord(range.x,range.x + range.width, displaySize, x);
            double yCoord = fractal.getCoord(range.y,range.y + range.height, displaySize, y);

            // Изменение приближения
            fractal.recenterAndZoomRange(range, xCoord, yCoord, e.getButton() == 1 ? 0.5 : 2);

            // повторная отрисовка фрактала
            drawFractal();
        }
    }



    // Метода main
    public static void main(String[] args) {
        // Инициализация фрактала
        FractalExplorer displayExplorer = new FractalExplorer(600);
        // Создание и отображение графического интерфейса приложения
        displayExplorer.createAndShowGUI();
        // Первичная отрисовка фрактала
        displayExplorer.drawFractal();
    }
}
