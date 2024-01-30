package fi.dy.masa.servux.network.packet;

/**
 * Converted from an Identifier-based enum list with the original comments from nnnik
 */
public class SyncmaticaPacketType
{
    public int SYNCMATICA_PROTOCOL_VERSION = 2;
    public String REGISTER_METADATA = "syncmatica:register_metadata";
    // one packet will be responsible for sending the entire metadata of a syncmatic
    // it marks the creation of a syncmatic - for now it also is responsible
    // for changing the syncmatic server and client side
    public String CANCEL_SHARE = "syncmatica:cancel_share";
    // send to a client when a share failed
    // the client can cancel the upload or upon finishing send a removal packet
    public String REQUEST_LITEMATIC = "syncmatica:request_download";
    // another group of packets will be responsible for downloading the entire
    // litematic starting with a download request
    public String SEND_LITEMATIC = "syncmatica:send_litematic";
    // a packet responsible for sending a bit of a litematic
    // (16 kilo-bytes to be precise (half of what minecraft can send in one packet at most))
    public String RECEIVED_LITEMATIC = "syncmatica:received_litematic";
    // a packet responsible for triggering another send for a litematic
    // by waiting until a response is sent I hope we can ensure
    // that we do not overwhelm the clients' connection to the server
    public String FINISHED_LITEMATIC = "syncmatica:finished_litematic";
    // a packet responsible for marking the end of a litematic
    // transmission
    public String CANCEL_LITEMATIC = "syncmatica:cancel_litematic";
    // a packet responsible for cancelling an ongoing upload/download
    // will be sent in several cases - upon errors mostly
    public String REMOVE_SYNCMATIC = "syncmatica:remove_syncmatic";
    // a packet that will be sent to clients when a syncmatic got removed
    // send to the server by a client if a specific client intends to remove a litematic from the server
    public String REGISTER_VERSION = "syncmatica:register_version";
    // this packet will be sent to the client when it joins the server
    // upon receiving this packet the client will check the server version
    // initializes syncmatica on the clients end
    // if it can function with the version on the server then it will respond with a version of its own
    // if the server can handle the client version the server will send
    public String CONFIRM_USER = "syncmatica:confirm_user";
    // the confirm-user packet
    // send after a successful version exchange
    // fully starts up syncmatica on the clients end
    // sends all server placements along to the client
    public String FEATURE_REQUEST = "syncmatica:feature_request";
    // requests the partner to send a list of its features
    // does not require a fully finished handshake
    public String FEATURE = "syncmatica:feature";
    // sends feature set to the partner
    // send during a version exchange to check if the 2 versions are compatible.
    // There is no default feature set available for the transmitted version
    // afterward the feature set is used to communicate to the partner
    public String MODIFY = "syncmatica:modify";
    // sends updated placement data to the client or vice versa
    public String MODIFY_REQUEST = "syncmatica:modify_request";
    // send from client to server to request the editing of placement values
    // used to ensure that only one person can edit at a time thus preventing all kinds of stuff
    public String MODIFY_REQUEST_DENY = "syncmatica:modify_request_deny";
    public String MODIFY_REQUEST_ACCEPT = "syncmatica:modify_request_accept";
    public String MODIFY_FINISH = "syncmatica:modify_finish";
    // send from client to server to mark that the editing of placement values has concluded
    // sends along the final data of the placement
    public String MESSAGE = "syncmatica:message";
    // sends a message from client to server - allows for future compatibility
    // can't fix the typo here lol
    // --> Don't worry, I fixed it. :)
}
