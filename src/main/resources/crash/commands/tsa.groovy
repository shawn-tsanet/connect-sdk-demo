import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Required
import org.crsh.cli.Usage

@Usage("TSANet Connect API demo (uses Spring ConnectApiClient in the running app)")
class tsa {

  private Object client() {
    def factory = context.attributes.factory
    return factory.getBean(Class.forName("com.tsanet.clientdemo.connectapi.ConnectApiClient"))
  }

  @Usage("log in to Connect API (username password)")
  @Command
  void login(
      @Usage("username") @Required @Argument String username,
      @Usage("password") @Required @Argument String password
  ) {
    try {
      client().login(username, password)
      out << "logged in as: ${username}\n"
    } catch (Exception ex) {
      err << "Login failed: ${ex.message}\n"
      throw ex
    }
  }

  @Usage("log in and print JWT only (alias for scripting)")
  @Command
  void apiLogin(
      @Usage("username") @Required @Argument String username,
      @Usage("password") @Required @Argument String password
  ) {
    def token = client().login(username, password)
    out << "${token}\n"
  }

  @Usage("clear Connect API session")
  @Command
  void logout() {
    client().logout()
    out << "logged out\n"
  }

  @Usage("alias for logout")
  @Command
  void apiLogout() {
    logout()
  }

  @Usage("show Connect API session state")
  @Command
  void session() {
    def c = client()
    out << "authorized: ${c.isAuthorized()}\n"
    def user = c.currentUsername()
    if (user.isPresent()) {
      out << "username: ${user.get()}\n"
    }
  }

  @Usage("fetch collaboration requests from Connect API")
  @Command
  void requests() {
    printRequests(client().getCollaborationRequests(), "Collaboration requests")
  }

  @Usage("fetch collaboration requests filtered by company id")
  @Command
  void requestsForCompany(@Usage("companyId") @Required @Argument Long companyId) {
    def all = client().getCollaborationRequests()
    def filtered = all.findAll { r ->
      companyId.equals(r.submitCompanyId()) || companyId.equals(r.receiveCompanyId())
    }
    printRequests(filtered, "Collaboration requests")
  }

  @Usage("list collaboration requests stored in SQLite")
  @Command
  void storedRequests() {
    printRequests(client().getStoredCollaborationRequests(), "Stored collaboration requests")
  }

  @Usage("list stored collaboration requests for company id")
  @Command
  void storedRequestsForCompany(@Usage("companyId") @Required @Argument Long companyId) {
    printRequests(client().getStoredCollaborationRequests(companyId), "Stored collaboration requests")
  }

  @Usage("fetch notes for all collaboration requests")
  @Command
  void notesAll() {
    printNotes(client().getNotesForAllRequests(), "Notes")
  }

  @Usage("fetch notes for one request token")
  @Command
  void notesForToken(@Usage("token") @Required @Argument String token) {
    printNotes(client().getNotes(token), "Notes")
  }

  @Usage("list notes stored in SQLite")
  @Command
  void storedNotes() {
    printNotes(client().getStoredNotes(), "Stored notes")
  }

  @Usage("list stored notes for request token")
  @Command
  void storedNotesForToken(@Usage("token") @Required @Argument String token) {
    printNotes(client().getStoredNotes(token), "Stored notes")
  }

  @Usage("fetch case responses for all collaboration requests")
  @Command
  void responsesAll() {
    printResponses(client().getResponsesForAllRequests(), "Case responses")
  }

  @Usage("fetch case responses for one request token")
  @Command
  void responsesForToken(@Usage("token") @Required @Argument String token) {
    printResponses(client().getResponses(token), "Case responses")
  }

  @Usage("list case responses stored in SQLite")
  @Command
  void storedResponses() {
    printResponses(client().getStoredResponses(), "Stored case responses")
  }

  @Usage("list stored responses for request token")
  @Command
  void storedResponsesForToken(@Usage("token") @Required @Argument String token) {
    printResponses(client().getStoredResponses(token), "Stored case responses")
  }

  @Usage("fetch current user from Connect API")
  @Command
  void me() {
    def user = client().getCurrentUser()
    out << "companyId=${user.companyId()} companyName=${user.companyName()} userId=${user.userId()} username=${user.username()} email=${user.email()}\n"
  }

  @Usage("show current user stored in SQLite")
  @Command
  void storedMe() {
    def users = client().getStoredCurrentUser()
    if (users.isEmpty()) {
      out << "No stored user context.\n"
      return
    }
    def user = users.get(0)
    out << "companyId=${user.companyId()} companyName=${user.companyName()} username=${user.username()} email=${user.email()}\n"
  }

  @Usage("fetch webhook subscriptions")
  @Command
  void webhooks() {
    printWebhooks(client().getWebhookSubscriptions(), "Webhook subscriptions")
  }

  @Usage("list webhook subscriptions stored in SQLite")
  @Command
  void storedWebhooks() {
    printWebhooks(client().getStoredWebhookSubscriptions(), "Stored webhook subscriptions")
  }

  @Usage("search partners")
  @Command
  void partners(@Usage("searchTerm") @Required @Argument String searchTerm) {
    printPartners(client().searchPartners(searchTerm), "Partners")
  }

  @Usage("list partners stored in SQLite")
  @Command
  void storedPartners() {
    printPartners(client().getStoredPartners(), "Stored partners")
  }

  @Usage("list stored partners for search term")
  @Command
  void storedPartnersForSearch(@Usage("searchTerm") @Required @Argument String searchTerm) {
    printPartners(client().getStoredPartners(searchTerm), "Stored partners")
  }

  @Usage("fetch collaboration request form for receiver company")
  @Command
  void form(@Usage("companyId") @Required @Argument Long companyId) {
    def form = client().getCollaborationRequestForm(companyId)
    out << "receiverCompanyId=${form.receiverCompanyId()} documentId=${form.documentId()} customFieldCount=${form.customFieldCount()}\n"
  }

  @Usage("create collaboration request (companyId caseNumber summary description)")
  @Command
  void createRequest(
      @Usage("companyId") @Required @Argument Long companyId,
      @Usage("caseNumber") @Required @Argument String caseNumber,
      @Usage("summary") @Required @Argument String summary,
      @Usage("description") @Required @Argument String description
  ) {
    def created = client().createCollaborationRequest(companyId, caseNumber, summary, description)
    printRequests([created], "Created collaboration request")
  }

  @Usage("sync requests, notes, and responses for all requests")
  @Command
  void sync() {
    client().syncAllRequestDetails()
    out << "Sync completed\n"
    printNotes(client().getStoredNotes(), "Stored notes")
    printResponses(client().getStoredResponses(), "Stored case responses")
  }

  private void printRequests(list, String title) {
    if (list == null || list.isEmpty()) {
      out << "No collaboration requests.\n"
      return
    }
    out << "${title} (${list.size()}):\n"
    list.each { r ->
      out << " - id=${r.id()} status=${r.status()} token=${r.token()} submitCompanyId=${r.submitCompanyId()} receiveCompanyId=${r.receiveCompanyId()} from=${r.submitCompanyName()} to=${r.receiveCompanyName()} summary=${r.summary()}\n"
    }
  }

  private void printNotes(list, String title) {
    if (list == null || list.isEmpty()) {
      out << "No notes.\n"
      return
    }
    out << "${title} (${list.size()}):\n"
    list.each { n ->
      out << " - id=${n.id()} caseToken=${n.caseToken()} token=${n.token()} status=${n.status()} summary=${n.summary()}\n"
    }
  }

  private void printResponses(list, String title) {
    if (list == null || list.isEmpty()) {
      out << "No responses.\n"
      return
    }
    out << "${title} (${list.size()}):\n"
    list.each { r ->
      out << " - id=${r.id()} caseToken=${r.caseToken()} type=${r.type()} engineer=${r.engineerName()} nextSteps=${r.nextSteps()}\n"
    }
  }

  private void printWebhooks(list, String title) {
    if (list == null || list.isEmpty()) {
      out << "No webhook subscriptions.\n"
      return
    }
    out << "${title} (${list.size()}):\n"
    list.each { w ->
      out << " - id=${w.id()} active=${w.active()} events=${w.eventTypes()} url=${w.callbackUrl()}\n"
    }
  }

  private void printPartners(list, String title) {
    if (list == null || list.isEmpty()) {
      out << "No partners.\n"
      return
    }
    out << "${title} (${list.size()}):\n"
    list.each { p ->
      out << " - search=${p.searchTerm()} label=${p.label()} company=${p.companyName()} companyId=${p.companyId()}\n"
    }
  }
}
