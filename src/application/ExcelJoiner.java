package application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import utils.Utils;

public class ExcelJoiner {

	public static void main(String... strings) throws InvalidFormatException, IOException {
		File metaDataFiles = new File(
				"C:\\Users\\Karlo Mendoza\\Box Sync\\Clients\\ICU Medical\\ICU Medical PLM Implementation\\Workstreams\\Program Data Migration\\Data Files\\MasterControl\\PreparationForRelease\\rename\\results\\");
		processData(metaDataFiles);
	}

	public static void processData(File metaDataFiles) throws InvalidFormatException, IOException {

		File[] listOfFiles = metaDataFiles.listFiles();
		try (SXSSFWorkbook writeIntoBook = new SXSSFWorkbook(100);) {
			Sheet writeSheet = writeIntoBook.createSheet("data");

			for (File file : listOfFiles) {
				if (file.getName().contains("results") || file.isDirectory() || file.getName().endsWith("txt") || file.getName().startsWith("~")) {
					continue;
				}

				try (Workbook wb = Utils.getWorkBook(file)) {
					Sheet readSheet = wb.getSheetAt(0);
					Row row;
					Row headerRow;

					// Load HeaderRow
					headerRow = readSheet.getRow(0);

					int rows = readSheet.getPhysicalNumberOfRows(); // No of rows
					int cols = 0; // No of columns
					int tmp = 0;

					// This trick ensures that we get the data properly even if it doesn't start
					// from first few rows
					for (int i = 0; i < 10 || i < rows; i++) {
						row = readSheet.getRow(i);
						if (row != null) {
							tmp = readSheet.getRow(i).getPhysicalNumberOfCells();
							if (tmp > cols)
								cols = tmp;
						}
					}

					if (writeSheet.getPhysicalNumberOfRows() == 0) {
						Row createRow = writeSheet.createRow(0);
						setCellsValuesToRow(createRow, headerRow, cols);
					}

					for (int r = 0; r < rows; r++) {
						row = readSheet.getRow(r);
						if (row != null) {
							// if it's not the header
							if (r > 0) {
								Row writeToRow = writeSheet.createRow(writeSheet.getPhysicalNumberOfRows());
								setCellsValuesToRow(writeToRow, row, cols);
							}
						}
					}

				}
			}
			File f = new File(metaDataFiles.getParentFile() + "\\Joined.xlsx");
			try (FileOutputStream outputStream = new FileOutputStream(f)) {
				writeIntoBook.write(outputStream);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Gets all the cells from dataRow and copys them in writeToRow, basically it copies the whole row, but skips the first one to allow to put the
	 * subClass
	 * 
	 * @param writeToRow
	 * @param dataRow
	 * @param colsNumber
	 *            number of columns to copy
	 */
	private static void setCellsValuesToRow(Row writeToRow, Row dataRow, int colsNumber) {
		for (int c = 0; c < colsNumber; c++) {
			Cell cell = dataRow.getCell((int) c);
			if (cell != null) {
				Cell createCell = writeToRow.createCell(c);

				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_NUMERIC:
					createCell.setCellValue(cell.getNumericCellValue());
					break;
				case Cell.CELL_TYPE_STRING:
					createCell.setCellValue(cell.getStringCellValue());
				}
			}
		}
	}
}
