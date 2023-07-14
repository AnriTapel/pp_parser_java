import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Xiaomi on 29.09.2020.
 */
public class App {

    private static List<String> allowedCategories = Arrays.asList(
            "https://posudapark.ru/product-category/nozhi-nozhnitsi/",
            "https://posudapark.ru/product-category/skovorodki/",
            "https://posudapark.ru/product-category/kastruli/",
            "https://posudapark.ru/product-category/chainiki/",
            "https://posudapark.ru/product-category/kovshi/",
            "https://posudapark.ru/product-category/nabori-posudi/",
            "https://posudapark.ru/product-category/parovarki-skorovarki/",
            "https://posudapark.ru/product-category/soteiniki/",
            "https://posudapark.ru/product-category/french-pressi-zavarniki/",
            "https://posudapark.ru/product-category/kofevarki-kofemolki/"
    );

    private static List<String> productsToSkipUrls = Arrays.asList(
            "https://posudapark.ru/product/nabor-posudy-iz-15-predmetov-royalty-line-rl-es1015m-red/",
            "https://posudapark.ru/product/marmit-dvojnoj-keramicheskij-so-steklyannoj-kryshkoj-2-13l-bekker-bk-7384/",
            "https://posudapark.ru/product/marmit-kruglyj-keramicheskij-so-steklyannoj-kryshkoj-45l-bekker-bk-7365/",
            "https://posudapark.ru/product/nabor-instrumentor-187-predmetov-v-chemodane-krafttechnik-kt-11188-rsbgs/",
            "https://posudapark.ru/product/nabor-instrumentor-187-predmetov-v-chemodane-krafttechnik-kt-11188abgs/",
            "https://posudapark.ru/product/nabor-posudy-iz-10-predmetov-meisterklasse-mk-1049/",
            "https://posudapark.ru/product/skovoroda-bez-kryshki-iz-litogo-alyuminiya-20sm-royalty-line-rl-fp20/",
            "https://posudapark.ru/product/skorovarka-iz-nerzhaveyushhej-stali-5-0l-22sm-bekker-bk-8903/",
            "https://posudapark.ru/product/skorovarka-iz-nerzhaveyushhej-stali-9-0l-26sm-bekker-bk-8905/",
            "https://posudapark.ru/product/chajnik-so-svistkom-30l-hoffmann-hm-5533-2/"
    );

    public static void main(String[] args) {
        ArrayList<Product> products = new ArrayList<Product>();
        for (String categoryUrl : App.allowedCategories) {
            try {
                int currentPageCounter = 1;
                int pagesCounter = 0;
                Document doc = Jsoup.connect(categoryUrl).get();
                pagesCounter = App.getPagesCount(doc);

                while (currentPageCounter <= pagesCounter) {
                    ArrayList<Product> newProducts;
                    if (currentPageCounter == 1) {
                        newProducts = App.parsePageData(doc, null, products);
                    } else {
                        newProducts = App.parsePageData(null, categoryUrl + "page/" + currentPageCounter + "/", products);
                    }
                    products.addAll(newProducts);
                    currentPageCounter++;
                }

                ExportToXls exporter = new ExportToXls(products);
                exporter.launchExportProcess();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static int getPagesCount(Document doc) {
        Element productsCountElement = doc.selectFirst("ul.page-numbers");
        if (productsCountElement == null) {
            return 1;
        }
        return Integer.parseInt(productsCountElement.child(productsCountElement.childNodeSize() / 2 - 2).select("a").text());
    }

    private static ArrayList<Product> parsePageData(Document doc, String url, ArrayList<Product> parsedProducts) {
        ArrayList<Product> products = new ArrayList<Product>();
        if (doc == null) {
            try {
                doc = Jsoup.connect(url).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Elements productsOnPage = doc.select("li.product");
            for (Element el : productsOnPage) {
                Product prod = App.getProductData(el, parsedProducts);
                if (prod != null) {
                    products.add(App.getProductData(el, parsedProducts));
                }
                Thread.sleep(1500);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return products;
    }

    private static Product getProductData(Element productElement, ArrayList<Product> parsedProducts) {
        Product product = new Product();
        try {
            String url = productElement.select(".woocommerce-LoopProduct-link").attr("href");
            if (App.productsToSkipUrls.contains(url)) {
                return null;
            }
            for (Product parsedProduct : parsedProducts) {
                if (parsedProduct.getProductUrl().equals(url)) {
                    return null;
                }
            }
            product.setProductUrl(url);
            Document doc = Jsoup.connect(url).get();
            StringBuilder images = new StringBuilder("");
            Elements imgElements = doc.select("figure.woocommerce-product-gallery__wrapper > div");
            int imgCount = 0;
            for (Element img : imgElements) {
                imgCount++;
                images.append(img.select("a").attr("href")).append(", ");
                if (imgCount > 3) {
                    break;
                }
            }
            if (images.length() <= 3) {
                return null;
            }

            product.setCategory(doc.selectFirst("span.posted_in > a").text());
            product.setImagesUrl(images.substring(0, images.length() - 2));
            product.setName(doc.selectFirst("h1.product_title.entry-title").text());
            product.setPrice(Integer.parseInt(doc.selectFirst("span.woocommerce-Price-amount.amount > bdi").ownText().replace(" ", "").trim()));
            String desc = doc.selectFirst("div.woocommerce-product-details__short-description > p").html();
            desc = desc.replaceAll("<br> ", " ");
            desc = desc.replaceAll("\n", " ");
            product.setDescription(desc);
            product.setSku(doc.select("span.sku").text());
            product.setId(Integer.parseInt(doc.select("span#custom_product_id").text()));

            try {
                product.setTradeMark(doc.selectFirst("tr.woocommerce-product-attributes-item--attribute_pa_trade_mark > td > p").text());
            } catch (Exception e){
                System.out.println("Product with sku" + product.getSku() + "has no trade mark");
            }

            try {
                product.setColor(doc.selectFirst("tr.woocommerce-product-attributes-item--attribute_pa_color > td > p").text());
            } catch (Exception e) {
                System.out.println("Product with sku" + product.getSku() + "has no color");
            }

            try {
                product.setMaterial(doc.selectFirst("tr.woocommerce-product-attributes-item--attribute_pa_material > td > p").text());
            } catch (Exception e){
                System.out.println("Product with sku" + product.getSku() + "has no material");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return product;
    }
}
