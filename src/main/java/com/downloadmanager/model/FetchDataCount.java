package com.downloadmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a job that gets the count of a given data items.
 * It downloads the total count only, not the data items themselves.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchDataCount extends AbstractFetch {

    long count;


}
