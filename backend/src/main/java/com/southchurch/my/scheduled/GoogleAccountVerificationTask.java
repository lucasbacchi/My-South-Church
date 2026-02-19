package com.southchurch.my.scheduled;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;
import com.southchurch.my.services.person.VerifyGoogleAccountService;

/**
 * Scheduled task to verify Google Accounts for all people in the database.
 * Runs every Sunday at 2 AM server time (cron: "0 0 2 * * SUN")
 */
@Component
public class GoogleAccountVerificationTask {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAccountVerificationTask.class);

    private final VerifyGoogleAccountService verifyGoogleAccountService;
    private final PeopleRepository peopleRepository;

    @Value("${google.account.verify.delay-ms:800}")
    private long delayMs;

    @Value("${google.account.verify.full-scan-interval-days:90}")
    private long fullScanIntervalDays;

    public GoogleAccountVerificationTask(
            VerifyGoogleAccountService verifyGoogleAccountService,
            PeopleRepository peopleRepository) {
        this.verifyGoogleAccountService = verifyGoogleAccountService;
        this.peopleRepository = peopleRepository;
    }

    /**
     * Runs every Sunday at 2 AM to verify all Google Accounts.
     * Only re-verifies accounts that are not already marked as verified
     * to minimize API calls.
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void verifyAllGoogleAccounts() {
        boolean shouldFullScan = fullScanIntervalDays > 0
                && (LocalDate.now().toEpochDay() % fullScanIntervalDays == 0);
        logger.info(
                "[GoogleAccountVerification] Starting weekly verification task (fullScan={}, delayMs={}, intervalDays={})",
                shouldFullScan, delayMs, fullScanIntervalDays);

        List<Person> people = peopleRepository.findAll();
        int verified = 0;
        int notVerified = 0;
        int indeterminate = 0;
        int skipped = 0;

        for (Person person : people) {
            // Skip verified accounts unless we're doing the monthly full scan.
            if (!shouldFullScan && Boolean.TRUE.equals(person.getGoogleAccountVerified())) {
                skipped++;
                continue;
            }

            try {
                Boolean result = verifyGoogleAccountService.verify(person.getPrimaryEmail());
                person.setGoogleAccountVerified(result);
                peopleRepository.save(person);

                if (Boolean.TRUE.equals(result)) {
                    verified++;
                } else if (Boolean.FALSE.equals(result)) {
                    notVerified++;
                } else {
                    indeterminate++;
                }
            } catch (Exception e) {
                logger.error("[GoogleAccountVerification] Error verifying {}: {}",
                        person.getPrimaryEmail(), e.getMessage());
                indeterminate++;
            }

            // Throttle to avoid API rate limits
            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    logger.warn("[GoogleAccountVerification] Verification task interrupted");
                    break;
                }
            }
        }

        logger.info(
                "[GoogleAccountVerification] Weekly verification complete: {} verified, {} not verified, {} indeterminate, {} skipped",
                verified, notVerified, indeterminate, skipped);
    }
}
