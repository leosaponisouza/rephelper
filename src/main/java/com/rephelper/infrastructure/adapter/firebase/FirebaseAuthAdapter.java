package com.rephelper.infrastructure.adapter.firebase;

import com.rephelper.application.dto.FirebaseUserInfo;
import org.springframework.stereotype.Component;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.rephelper.domain.exception.AuthenticationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FirebaseAuthAdapter {

    private final FirebaseAuth firebaseAuth;

    public String verifyToken(String token) {
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
            return decodedToken.getUid();
        } catch (FirebaseAuthException e) {
            log.error("Firebase token verification failed", e);
            throw new AuthenticationException("Invalid or expired Firebase token: " + e.getMessage());
        }
    }

    public FirebaseUserInfo getUserInfo(String uuid) {
        try {
            UserRecord userRecord = firebaseAuth.getUser(uuid);

            // Safely handle provider data
            String provider = "unknown";
            if (userRecord.getProviderData() != null && !(userRecord.getProviderData().length == 0)) {
                provider = userRecord.getProviderData()[0].getProviderId();
            }

            return FirebaseUserInfo.builder()
                    .uuid(userRecord.getUid())
                    .email(userRecord.getEmail())
                    .displayName(userRecord.getDisplayName())
                    .photoUrl(userRecord.getPhotoUrl())
                    .phoneNumber(userRecord.getPhoneNumber())
                    .emailVerified(userRecord.isEmailVerified())
                    .provider(provider)
                    .build();
        } catch (FirebaseAuthException e) {
            log.error("Error fetching user info from Firebase", e);
            throw new AuthenticationException("Error fetching user info from Firebase: " + e.getMessage());
        }
    }
}