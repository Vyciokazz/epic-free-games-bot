package me.hax3.epic.page;

import me.hax3.epic.model.GameStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static org.openqa.selenium.support.ui.ExpectedConditions.numberOfElementsToBeMoreThan;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

@Component
public class GamePage extends BasePage {

    private static final String XPATH_OF_GET_BUTTON = "//button//span[text()='Get']/../..";
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final WebDriver webDriver;
    private final WebDriverWait webDriverWait;

    public GamePage(WebDriver webDriver) {
        super(webDriver);
        this.webDriver = webDriver;
        webDriverWait = new WebDriverWait(webDriver, 30);
    }

    public int getNumberOfFreeGame() {
        try {
            webDriverWait.withTimeout(Duration.ofSeconds(5)).until(numberOfElementsToBeMoreThan(By.xpath("//a//span[text()='Free']"), 0));
        } catch (TimeoutException e) {
            log.info("Only one game available for this week.");
            return 1;
        }
        return webDriver.findElements(By.xpath("//a//span[text()='Free']")).size();
    }

    public GameStatus getStatus() {
        webDriverWait.until(visibilityOfElementLocated(By.xpath("//button//span[text()='Get' or text()='Owned']")));
        final String discount = webDriver.findElement(By.xpath("//span[text()='Free']")).getText();
        final String price = webDriver.findElement(By.xpath("//span[text()='Free']/preceding-sibling::s")).getText();
        final String owned = webDriver.findElement(By.xpath("//button//span[text()='Get' or text()='Owned']/../..")).getText();

        return new GameStatus(parseGameName(webDriver.getCurrentUrl()), price, parseDiscount(discount), parseOwned(owned));
    }

    public void clickGet() {
        final String getButton = XPATH_OF_GET_BUTTON;
        if (webDriver.findElements(By.xpath(getButton)).size() > 1) {
            webDriver.findElement(By.xpath("//div[@id=\"editions\"]/following-sibling::*//button//span[text()='Get']/../.."))
                .click();
        } else {
            webDriver.findElement(By.xpath(getButton)).click();
        }
    }

    private String parseGameName(String url) {
        return url
            .replace("https://www.epicgames.com/store/en-US/", "")
            .replace("bundles/", "")
            .replace("product/", "")
            .replace("/home", "");
    }

    private boolean parseDiscount(String discount) {
        return discount.equalsIgnoreCase("Free");
    }

    private boolean parseOwned(String owned) {
        return owned.equalsIgnoreCase("OWNED");
    }

    public void clickFree(int index) {
        webDriver.findElements(By.xpath("//a//span[text()='Free']")).get(index).click();
    }

    public void clickContinueIfItPresented() {
        try {
            final By xpathOfContinue = By.xpath("//button/span[text()='Continue']/..");
            webDriverWait.withTimeout(Duration.ofSeconds(3)).until(visibilityOfElementLocated(xpathOfContinue));
            webDriver.findElement(xpathOfContinue).click();
        } catch (TimeoutException e) {
            log.info("Continue button is not presented");
        }
    }

    public void clickGetAddon() {
        webDriver.findElement(By.xpath(XPATH_OF_GET_BUTTON)).click();
    }

    public int getNumberOfAddons() {
        return webDriver.findElements(By.xpath(XPATH_OF_GET_BUTTON)).size();
    }
}
