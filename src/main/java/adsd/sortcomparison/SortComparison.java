package adsd.sortcomparison;

import java.io.*;
import java.util.*;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;
import javax.swing.SwingUtilities; 

public class SortComparison extends JFrame {

    // --- 1. Quick Sort Implementation ---
    public static void quickSort(String[] arr, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(arr, low, high);
            quickSort(arr, low, pivotIndex - 1);
            quickSort(arr, pivotIndex + 1, high);
        }
    }

    private static int partition(String[] arr, int low, int high) {
        String pivot = arr[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (arr[j].compareToIgnoreCase(pivot) <= 0) {
                i++;
                String temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }

        String temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1;
    }

    // --- 2. Bubble Sort Implementation ---
    public static void bubbleSort(String[] arr) {
        int n = arr.length;
        boolean swapped;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j].compareToIgnoreCase(arr[j + 1]) > 0) {
                    String temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped)
                break;
        }
    }

    // --- 3. Fungsi Pengukuran Waktu ---
    public static long measureTime(String[] data, String sortType) {
        String[] arrCopy = Arrays.copyOf(data, data.length);
        long startTime = System.nanoTime();
        
        if (sortType.equals("Quick")) {
            quickSort(arrCopy, 0, arrCopy.length - 1);
        } else if (sortType.equals("Bubble")) {
            bubbleSort(arrCopy);
        }
        
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000; // Waktu dalam milidetik (ms)
    }
    
    // --- 4. Fungsi Pembacaan Data & Subset ---
    
    /** Membaca SEMUA data dari CSV */
    public static String[] readAllData(String filePath) throws IOException {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > 0) {
                    String nama = values[0].trim().replaceAll("[\"']", "");
                    if (!nama.isEmpty()) {
                        list.add(nama);
                    }
                }
            }
        }
        return list.toArray(new String[0]);
    }
    
    /** Mengambil subset data untuk pengujian multi-ukuran */
    public static String[] getSubset(String[] data, int size) {
        return Arrays.copyOf(data, Math.min(size, data.length));
    }
    
    // --- Konstruktor JFreeChart ---
    // --- Ganti Konstruktor JFreeChart menjadi Bar Chart ---
    public SortComparison(String title, DefaultCategoryDataset dataset) {
        super(title);
        // Menggunakan createBarChart untuk perbandingan satu titik data
        JFreeChart barChart = ChartFactory.createBarChart(
           "Perbandingan Waktu Eksekusi (N=225)", // Judul Utama
           "Algoritma", // Label Sumbu X (Kategori)
           "Waktu Eksekusi Rata-rata (ms)", // Label Sumbu Y (Nilai)
           dataset, // Data
           org.jfree.chart.plot.PlotOrientation.VERTICAL,
           true, true, false
        );
            
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 400));
        setContentPane(chartPanel);
    }
    
    public static void writeCSV(String filePath, String[] data) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String s : data) {
            bw.write(s);
            bw.newLine();
            }
        } catch (IOException e) {
        System.out.println("\n=> Gagal menulis file CSV: " + e.getMessage());
        }
    }

    // --- Ganti Fungsi Main (Hanya Uji N=225) ---
    public static void main(String[] args) {
        String filePath = "datasi2025.csv"; 
        String[] allData;
        
        try {
            allData = readAllData(filePath);
            System.out.println("=> Data berhasil dibaca. Total data: " + allData.length);

        } catch (IOException e) {
            System.out.println("=> Gagal membaca file: " + e.getMessage());
            return; 
        }

        // Karena data hanya 225, kita hanya menguji N=225
        int size = allData.length; 
        
        // Buat dataset khusus untuk Bar Chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Random rand = new Random();

        System.out.println("\n--- PERBANDINGAN WAKTU (N=" + size + ") ---");
        
        // 1. Ambil data asli dan acak (untuk perbandingan yang adil)
        List<String> list = Arrays.asList(allData);
        Collections.shuffle(list, rand);
        String[] shuffledData = list.toArray(new String[0]);

        // --- Ukur Quick Sort ---
        long timeQuick = measureTime(shuffledData, "Quick");
        dataset.addValue(timeQuick, "Quick Sort", "Quick Sort (O(N log N))");

        // --- Ukur Bubble Sort ---
        long timeBubble = measureTime(shuffledData, "Bubble");
        dataset.addValue(timeBubble, "Bubble Sort", "Bubble Sort (O(N^2))");

        System.out.println("1. Waktu Quick Sort: " + timeQuick + " ms");
        System.out.println("2. Waktu Bubble Sort: " + timeBubble + " ms");

        // Buat copy lalu sort
        String[] sortedData = Arrays.copyOf(shuffledData, shuffledData.length);
        quickSort(sortedData, 0, sortedData.length - 1);
        
        // === Simpan CSV baru berisi data yang sudah tersusun ===
        String outputSorted = "datasi2025_sorted.csv";
        writeCSV(outputSorted, sortedData);
        System.out.println("\n=> CSV sorted berhasil dibuat: " + outputSorted);
        
        // --- Tampilkan Grafik Batang ---
        SwingUtilities.invokeLater(() -> {
            SortComparison chart = new SortComparison("Perbandingan Algoritma Sorting", dataset);
            chart.pack(); 
            RefineryUtilities.centerFrameOnScreen(chart); 
            chart.setVisible(true); 
            chart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }
}