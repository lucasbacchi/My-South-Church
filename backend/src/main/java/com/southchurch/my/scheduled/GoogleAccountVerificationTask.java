package com.southchurch.my.scheduled;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        logger.info("[GoogleAccountVerification] Starting weekly verification task");

        List<Person> people = peopleRepository.findAll();
        int verified = 0;
        int notVerified = 0;
        int indeterminate = 0;
        int skipped = 0;

        for (Person person : people) {
            // Skip if already verified (to save API calls)
            if (Boolean.TRUE.equals(person.getGoogleAccountVerified())) {
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
        }

        logger.info(
                "[GoogleAccountVerification] Weekly verification complete: {} verified, {} not verified, {} indeterminate, {} skipped",
                verified, notVerified, indeterminate, skipped);
    }
}
