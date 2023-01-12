package pl.terra.cloud_iot.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.terra.http.api.OnboardingApi;

@RestController
public class OnboardingController implements OnboardingApi {

    @Override
    public ResponseEntity<Void> addDeviceToPoolList(Long userId, String deviceCode) throws Exception {
        return OnboardingApi.super.addDeviceToPoolList(userId, deviceCode);
    }
}
