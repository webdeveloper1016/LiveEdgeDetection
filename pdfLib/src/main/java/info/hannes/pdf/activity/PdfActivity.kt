package info.hannes.pdf.activity


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import info.hannes.pdf.R


class PdfActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pdf)
    }

    // https://stackoverflow.com/questions/14811618/add-image-to-a-pdf-using-itext

//    May this help you for adding image and also image caption to pdf.
//
//    Image image1 = Image.getInstance("sample.png");
//    image1.setAlignment(Element.ALIGN_CENTER);
//    image1.scaleAbsolute(450, 250);
////Add to document
//    document.add(image1);
//    Paragraph imgCaption = new Paragraph(" Sample image-1", imgcaption);
//    Summary.setAlignment(Element.ALIGN_LEFT);
//    Summary.setSpacingAfter(10f);
//    document.add(imgCaption);


//    fun toPdf() {
//        val conv = File("Report Forms/")
//        val files = conv.listFiles()
//        for (c in files.indices) {
//            var writer: PdfWriter?
//            try {
//                val output = Document(PageSize.B4)
//                val page: Rectangle = output.pageSize
//                page.setBackgroundColor(Color(255, 255, 255))
//                val fs = FileInputStream("Report Forms/" + files[c])
//                val file = FileOutputStream(File("Report Forms/" + name.toString() + " " + exa.toString() + " FORM " + level.toString() + " " + year.toString() + ".pdf"))
//                val `in` = DataInputStream(fs)
//                val br = BufferedReader(InputStreamReader(`in`))
//                writer = PdfWriter.getInstance(output, file)
//                writer.createXmpMetadata()
//
//                //define the  font for the disclaimer.
//                val fontfooter: Font = Font(Font.COURIER, 12, Font.BOLD)
//                fontfooter.setColor(java.awt.Color(0, 52, 154))
//                output.newPage()
//                writer.setPageEvent(CustomBorder())
//                output.open()
//                writer.open()
//                val fsname = FileInputStream("Report Forms/" + files[c])
//                val brname = BufferedReader(InputStreamReader(fsname))
//                for (p in 0 downTo -1) {
//                    brname.readLine()
//                }
//                val custname: String = brname.readLine()
//                // System.out.println(custname);
//                val f1: com.lowagie.text.Font = Font(com.lowagie.text.Font.COURIER, 12, com.lowagie.text.Font.BOLD)
//                f1.setColor(Color.BLACK)
//                output.add(Paragraph(Phrase(custname, f1)))
//                val image: Image = Image.getInstance("Student Images/" + num.toString() + ".png")
//                image.setAlignment(Image.ALIGN_RIGHT)
//                image.setAbsolutePosition(450f, 10f)
//                image.scalePercent(60, 50)
//                val chunk = Chunk(image, 0, -20)
//                val header = HeaderFooter(Phrase(chunk), false)
//                header.setAlignment(Element.ALIGN_RIGHT)
//                header.setBorder(Rectangle.NO_BORDER)
//                output.setHeader(header)
//                var line: String? = ""
//                var lineNo: Int = br.read()
//                while (br.readLine().also { line = it } != null) {
//                    lineNo = 0
//                    while (br.ready()) {
//                        if (lineNo % 2 == 1) {
//                            line = br.readLine()
//                            val f2: com.lowagie.text.Font = Font(com.lowagie.text.Font.COURIER, 12, com.lowagie.text.Font.BOLD)
//                            f2.setColor(Color.BLACK)
//                            val p1 = Paragraph(line, f2)
//                            p1.setAlignment(Element.TABLE)
//                            val table = PdfPTable(1)
//                            table.setWidthPercentage(100)
//                            val cell = PdfPCell()
//                            cell.setBackgroundColor(java.awt.Color(247, 246, 243))
//                            cell.setBorder(Rectangle.NO_BORDER)
//                            cell.setUseBorderPadding(true)
//                            cell.setPadding(0)
//                            cell.setBorderColor(java.awt.Color(247, 246, 243))
//                            cell.addElement(p1)
//                            table.addCell(cell)
//                            output.add(table)
//                        } else {
//                            line = br.readLine()
//                            val f2: com.lowagie.text.Font = Font(com.lowagie.text.Font.COURIER, 12, com.lowagie.text.Font.BOLD)
//                            f2.setColor(java.awt.Color(0, 52, 154))
//                            val p1 = Paragraph(line, f2)
//                            p1.setAlignment(Element.TABLE)
//                            val table = PdfPTable(1)
//                            table.setWidthPercentage(100)
//                            val cell = PdfPCell()
//                            cell.setBorder(Rectangle.NO_BORDER)
//                            cell.setBorderWidthBottom(1f)
//                            cell.setUseBorderPadding(true)
//                            cell.setPadding(0)
//                            cell.setBorderColor(java.awt.Color(255, 255, 255))
//                            cell.addElement(p1)
//                            table.addCell(cell)
//                            // output.add(page);
//                            output.add(table)
//                        }
//                        lineNo++
//                    }
//                }
//                output.close()
//                file.close()
//                fs.close()
//                fsname.close()
//                `in`.close()
//                writer.close()
//            } catch (ce: Exception) {
//                JOptionPane.showMessageDialog(this, ce, "Error", JOptionPane.PLAIN_MESSAGE)
//            }
//        }
//    }

}