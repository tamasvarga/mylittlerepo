package hu.grape.nkmrtd.utils;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class GCPStorageUtils {

    public static final String DRL_DIRECTORY_URL = String.format("%s/%s/", "https://storage.googleapis.com", RtdConstants.BUCKET_NAME_IN_STORAGE_FOR_RULES);
    private static final String DRL_TYPE = "application/octet-stream";

    public static List<String> readRulesFromGCPStorage() {
        log.debug("Reading rules from GCP storage.");
        final List<String> ruleNames = new LinkedList<>();
        final Storage storage = StorageOptions.getDefaultInstance().getService();

        final Bucket bucket = storage.get(RtdConstants.BUCKET_NAME_IN_STORAGE_FOR_RULES);
        final Page<Blob> blobs = bucket.list();
        for (final Blob blob : blobs.iterateAll()) {
            if (blob.getName().startsWith(RtdConstants.FOLDER_NAME_IN_STORAGE_FOR_RULES)
                    && DRL_TYPE.equals(blob.getContentType())) {
                ruleNames.add(blob.getName());
            }
        }

        log.info("Rules read from GCP storage successfully. > rules: {}", Arrays.toString(ruleNames.toArray()));
        return ruleNames;
    }
}
