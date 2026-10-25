package com.patelsamaj.patelsamaj_backend.service.impl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import com.patelsamaj.patelsamaj_backend.dto.EventDTO;
import com.patelsamaj.patelsamaj_backend.dto.ItemSelectionDTO;
import com.patelsamaj.patelsamaj_backend.service.PdfGenerationService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private final TemplateEngine templateEngine;

    private String notoSansRegularBase64;
    private String notoSansBoldBase64;

    private String getFontAsBase64(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            byte[] fontBytes = resource.getInputStream().readAllBytes();
            return Base64.getEncoder().encodeToString(fontBytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font: " + resourcePath, e);
        }
    }

    private synchronized void initFonts() {
        if (notoSansRegularBase64 == null) {
            try {
                notoSansRegularBase64 = getFontAsBase64("fonts/NotoSansGujarati-Regular.ttf");
            } catch (Exception ignored) {}
            try {
                notoSansBoldBase64 = getFontAsBase64("fonts/NotoSansGujarati-Bold.ttf");
            } catch (Exception ignored) {}
        }
    }

    private static final String[] WORDS = {
            "શૂન્ય",
            "એક", "બે", "ત્રણ", "ચાર", "પાંચ", "છ", "સાત", "આઠ", "નવ",
            "દસ", "અગિયાર", "બાર", "તેર", "ચૌદ", "પંદર", "સોળ", "સત્તર", "અઢાર", "ઓગણીસ",
            "વીસ", "એકવીસ", "બાવીસ", "તેવીસ", "ચોવીસ", "પચ્ચીસ", "છવ્વીસ", "સત્તાવીસ", "અઠ્ઠાવીસ", "ઓગણત્રીસ",
            "ત્રીસ", "એકત્રીસ", "બત્રીસ", "તેત્રીસ", "ચોત્રીસ", "પાંત્રીસ", "છત્રીસ", "સડત્રીસ", "આડત્રીસ", "ઓગણચાલીસ",
            "ચાલીસ", "એકતાલીસ", "બેતાલીસ", "ત્રેતાલીસ", "ચુમ્માલીસ", "પિસ્તાલીસ", "છેતાલીસ", "સુડતાલીસ", "અડતાલીસ", "ઓગણપચાસ",
            "પચાસ", "એકાવન", "બાવન", "ત્રેપન", "ચોપન", "પંચાવન", "છપ્પન", "સત્તાવન", "અઠ્ઠાવન", "ઓગણસાઠ",
            "સાઠ", "એકસઠ", "બાસઠ", "ત્રેસઠ", "ચોસઠ", "પાંસઠ", "છાસઠ", "સડસઠ", "અડસઠ", "ઓગણસિત્તેર",
            "સિત્તેર", "એકોતેર", "બોતેર", "તેરોતેર", "ચુમોતેર", "પંચોતેર", "છોતેર", "સિત્યોતેર", "અઠ્યોતેર", "ઓગણએંસી",
            "એંસી", "એક્યાસી", "બ્યાસી", "ત્ર્યાસી", "ચોર્યાસી", "પંચ્યાસી", "છ્યાસી", "સિત્યાસી", "અઠ્યાસી", "નેવ્યાસી",
            "નેવું", "એકાણું", "બાણું", "ત્રાણું", "ચોરાણું", "પંચાણું", "છન્નું", "સત્તાણું", "અઠ્ઠાણું", "નવ્વાણું"
    };

    private static final String[] HUNDREDS = {"", "એકસો", "બસો", "ત્રણસો", "ચારસો", "પાંચસો", "છસો", "સાતસો", "આઠસો", "નવસો" };

    private static final long[] VALUES = {
            1000000000L, // અરબ
            10000000L,   // કરોડ
            100000L,     // લાખ
            1000L         // હજાર
    };

    private static final String[] NAMES = { "અરબ", "કરોડ", "લાખ", "હજાર" };

    private static final char[] GUJARATI_DIGITS = {
            '૦', '૧', '૨', '૩', '૪', '૫', '૬', '૭', '૮', '૯'
    };

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BillRow {
        private String srNoLeft;
        private String nameLeft;
        private String rateLeft;
        private String qtyLeft;
        private String rupeesLeft;

        private String srNoRight;
        private String nameRight;
        private String rateRight;
        private String qtyRight;
        private String rupeesRight;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BillPage {
        private int pageNumber;
        private int totalPages;
        private boolean isFirstPage;
        private boolean isLastPage;
        private String pageNumberGujarati;
        private String nextPageNumberGujarati;
        private List<BillRow> rows;
    }

    private String toGujaratiDigits(Object input) {
        if (input == null) {
            return "";
        }
        String str = String.valueOf(input);
        StringBuilder result = new StringBuilder();
        for (char ch : str.toCharArray()) {
            if (ch >= '0' && ch <= '9') {
                result.append(GUJARATI_DIGITS[ch - '0']);
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private String formatDateToGujarati(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return toGujaratiDigits(date.format(formatter));
    }

    private String toGujaratiWords(long number) {
        if (number == 0) {
            return WORDS[0] + " પૂરા";
        }
        return convert(number).trim() + " પૂરા";
    }

    private static String convert(long number) {
        if (number < 100) {
            return WORDS[(int) number];
        }

        if (number < 1000) {
            int hundreds = (int) (number / 100);
            int remainder = (int) (number % 100);

            if (remainder == 0)
                return HUNDREDS[hundreds];

            return HUNDREDS[hundreds] + " " + convert(remainder);
        }

        for (int i = 0; i < VALUES.length; i++) {
            if (number >= VALUES[i]) {
                long quotient = number / VALUES[i];
                long remainder = number % VALUES[i];

                String result = convert(quotient) + " " + NAMES[i];

                if (remainder != 0)
                    result += " " + convert(remainder);

                return result;
            }
        }

        return "";
    }

    private Browser launchBrowser(Playwright playwright) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of("--no-sandbox", "--disable-setuid-sandbox", "--disable-dev-shm-usage", "--disable-gpu"));

        try {
            options.setChannel("chrome");
            return playwright.chromium().launch(options);
        } catch (Exception e) {
            try {
                options.setChannel("msedge");
                return playwright.chromium().launch(options);
            } catch (Exception ex) {
                options.setChannel((String) null);
                return playwright.chromium().launch(options);
            }
        }
    }

    @Override
    public byte[] generateBillPdf(EventDTO event) {
        initFonts();

        // 1. Prepare Thymeleaf processing context variables
        Context context = new Context();
        context.setVariable("event", event);
        context.setVariable("notoSansRegularBase64", notoSansRegularBase64);
        context.setVariable("notoSansBoldBase64", notoSansBoldBase64);

        List<ItemSelectionDTO> items = event.getItems() != null ? event.getItems() : new ArrayList<>();
        final int ROWS_PER_PAGE = 35;
        final int ITEMS_PER_PAGE = ROWS_PER_PAGE * 2;

        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE));
        List<BillPage> pages = new ArrayList<>(totalPages);

        for (int p = 0; p < totalPages; p++) {
            int itemOffset = p * ITEMS_PER_PAGE;
            boolean isFirstPage = (p == 0);
            boolean isLastPage = (p == totalPages - 1);

            int itemsRemaining = Math.max(0, items.size() - itemOffset);
            int rowsForThisPage = Math.min(ROWS_PER_PAGE, (int) Math.ceil((double) itemsRemaining / 2.0));

            List<BillRow> pageRows = new ArrayList<>(rowsForThisPage);

            for (int r = 0; r < rowsForThisPage; r++) {
                BillRow.BillRowBuilder rowBuilder = BillRow.builder();

                // Left side
                int leftIndex = itemOffset + r;
                if (leftIndex < items.size()) {
                    rowBuilder.srNoLeft(toGujaratiDigits(leftIndex + 1));
                    ItemSelectionDTO leftItem = items.get(leftIndex);
                    rowBuilder.nameLeft(leftItem.getItemName() != null ? leftItem.getItemName() : "");
                    if (leftItem.getQuantity() != null && leftItem.getQuantity() > 0) {
                        int price = leftItem.getPriceAtBooking();
                        int rupees = price * leftItem.getQuantity();

                        rowBuilder.rateLeft(toGujaratiDigits(price));
                        rowBuilder.qtyLeft(toGujaratiDigits(leftItem.getQuantity()));
                        rowBuilder.rupeesLeft(toGujaratiDigits(rupees));
                    } else {
                        rowBuilder.rateLeft("");
                        rowBuilder.qtyLeft("");
                        rowBuilder.rupeesLeft("");
                    }
                } else {
                    rowBuilder.srNoLeft("");
                    rowBuilder.nameLeft("");
                    rowBuilder.rateLeft("");
                    rowBuilder.qtyLeft("");
                    rowBuilder.rupeesLeft("");
                }

                // Right side
                int rightIndex = itemOffset + rowsForThisPage + r;
                if (rightIndex < items.size()) {
                    rowBuilder.srNoRight(toGujaratiDigits(rightIndex + 1));
                    ItemSelectionDTO rightItem = items.get(rightIndex);
                    rowBuilder.nameRight(rightItem.getItemName() != null ? rightItem.getItemName() : "");
                    if (rightItem.getQuantity() != null && rightItem.getQuantity() > 0) {
                        int price = rightItem.getPriceAtBooking();
                        int rupees = price * rightItem.getQuantity();

                        rowBuilder.rateRight(toGujaratiDigits(price));
                        rowBuilder.qtyRight(toGujaratiDigits(rightItem.getQuantity()));
                        rowBuilder.rupeesRight(toGujaratiDigits(rupees));
                    } else {
                        rowBuilder.rateRight("");
                        rowBuilder.qtyRight("");
                        rowBuilder.rupeesRight("");
                    }
                } else {
                    rowBuilder.srNoRight("");
                    rowBuilder.nameRight("");
                    rowBuilder.rateRight("");
                    rowBuilder.qtyRight("");
                    rowBuilder.rupeesRight("");
                }

                pageRows.add(rowBuilder.build());
            }

            pages.add(BillPage.builder()
                    .pageNumber(p + 1)
                    .totalPages(totalPages)
                    .isFirstPage(isFirstPage)
                    .isLastPage(isLastPage)
                    .pageNumberGujarati(toGujaratiDigits(p + 1))
                    .nextPageNumberGujarati(toGujaratiDigits(p + 2))
                    .rows(pageRows)
                    .build());
        }

        context.setVariable("pages", pages);
        context.setVariable("eventDateGujarati", formatDateToGujarati(event.getStartDate()));
        context.setVariable("contactNumberGujarati", toGujaratiDigits(event.getContactNumber()));
        context.setVariable("billNumberGujarati", toGujaratiDigits(event.getEventId()));

        int totalAmount = event.getTotalAmount() != null ? event.getTotalAmount() : 0;
        context.setVariable("totalAmountInWords", toGujaratiWords(totalAmount));
        context.setVariable("totalAmountGujaratiDigits", toGujaratiDigits(totalAmount));

        // 3. Process data values through the Thymeleaf template file
        String processedHtml = templateEngine.process("bill-template", context);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = launchBrowser(playwright);
            try {
                Page page = browser.newPage();
                page.setContent(
                        processedHtml,
                        new Page.SetContentOptions().setWaitUntil(WaitUntilState.LOAD)
                );

                // Wait until embedded fonts have finished loading
                page.evaluate("() => document.fonts.ready");

                Page.PdfOptions pdfOptions = new Page.PdfOptions()
                        .setFormat("A4")
                        .setPrintBackground(true)
                        .setPreferCSSPageSize(true);

                return page.pdf(pdfOptions);
            } finally {
                browser.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate bill PDF using Playwright", e);
        }
    }
}
