package com.cloudnut.webterm.thirdparty.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwoWayMessage {
    // FROM SERVER TO CLIENT
    // type     e/n/s/m   encoded/string/size/joinedUserList
    // payload  string    encoded data/string/json of ROWCOL/string
    //
    // FROM CLIENT TO SERVER
    // type     connect/resize/suspend
    // payload  TokenRowsCols/RowCol/Token
    //
    private String type;
    private String payload;
}
