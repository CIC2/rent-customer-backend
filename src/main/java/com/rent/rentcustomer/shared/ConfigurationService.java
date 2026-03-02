package com.resale.loveresalecustomer.shared;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.resale.loveresalecustomer.model.ConfigCustomersActivity;
import com.resale.loveresalecustomer.model.Configuration;
import com.resale.loveresalecustomer.repository.ConfigCustomersActivityRepository;
import com.resale.loveresalecustomer.repository.ConfigurationRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConfigurationService {

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private ConfigCustomersActivityRepository ConfigCustomersActivityRepository;

    public Optional<Configuration> getActiveConfig(String key) {
        return configurationRepository.findByConfigKeyAndIsActiveTrue(key);
    }

    public boolean shouldShowConfig(Long customerId, String key) {
        Optional<Configuration> config = getActiveConfig(key);
        if (config.isEmpty()) return false;

//        if (!"per_day_per_user".equalsIgnoreCase(config.get().getConditionType()))
//            return true;

        // Check last shown time
        Optional<ConfigCustomersActivity> activity =
                ConfigCustomersActivityRepository.findByCustomerIdAndConfigKey(customerId, key);

        if (activity.isEmpty()) return true; // never shown before

        LocalDateTime lastShown = activity.get().getLastShownAt();
        return lastShown.isBefore(LocalDateTime.now().minusDays(1));
    }

    public void markConfigShown(Long userId, String key) {
        ConfigCustomersActivityRepository.findByCustomerIdAndConfigKey(userId, key)
                .ifPresentOrElse(activity -> {
                    activity.setLastShownAt(LocalDateTime.now());
                    ConfigCustomersActivityRepository.save(activity);
                }, () -> {
                    ConfigCustomersActivity newActivity = new ConfigCustomersActivity();
                    newActivity.setCustomerId(userId);
                    newActivity.setConfigKey(key);
                    newActivity.setLastShownAt(LocalDateTime.now());
                    ConfigCustomersActivityRepository.save(newActivity);
                });
    }
}
