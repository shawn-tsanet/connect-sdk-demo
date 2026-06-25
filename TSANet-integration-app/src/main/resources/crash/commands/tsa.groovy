import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Required
import org.crsh.cli.Usage

@Usage("TSANet Connect API facade (uses connect-library via Spring)")
class tsa {

  private Object apiSession() {
    def factory = context.attributes.factory
    return factory.getBean(Class.forName("com.tsanet.api.TsaNetApiSession"))
  }

  @Usage("log in to Connect API (username password)")
  @Command
  void login(
      @Usage("username") @Required @Argument String username,
      @Usage("password") @Required @Argument String password
  ) {
    try {
      apiSession().auth().login(username, password)
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
    def token = apiSession().auth().login(username, password)
    out << "${token}\n"
  }

  @Usage("log in with credentials from application.yml")
  @Command
  void loginConfigured() {
    try {
      apiSession().auth().loginWithConfiguredCredentials()
      def username = apiSession().auth().currentUsername().orElse("configured user")
      out << "logged in as: ${username}\n"
    } catch (Exception ex) {
      err << "Login failed: ${ex.message}\n"
      throw ex
    }
  }

  @Usage("print current bearer token")
  @Command
  void token() {
    def token = apiSession().auth().currentBearerToken()
    if (token.isPresent()) {
      out << "${token.get()}\n"
      return
    }
    out << "No bearer token in session.\n"
  }

  @Usage("clear Connect API session")
  @Command
  void logout() {
    apiSession().auth().logout()
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
    def auth = apiSession().auth()
    out << "authorized: ${auth.isAuthorized()}\n"
    def user = auth.currentUsername()
    if (user.isPresent()) {
      out << "username: ${user.get()}\n"
    }
  }

  @Usage("fetch collaboration requests from Connect API")
  @Command
  void requests() {
    printRequests(apiSession().collaborationRequests().listRequests(), "Collaboration requests")
  }

  @Usage("fetch collaboration requests filtered by company id")
  @Command
  void requestsForCompany(@Usage("companyId") @Required @Argument Long companyId) {
    def all = apiSession().collaborationRequests().listRequests()
    def filtered = all.findAll { r ->
      companyId.equals(r.submitCompanyId()) || companyId.equals(r.receiveCompanyId())
    }
    printRequests(filtered, "Collaboration requests")
  }

  @Usage("list collaboration requests stored in SQLite")
  @Command
  void storedRequests() {
    printRequests(apiSession().collaborationRequests().listStoredRequests(), "Stored collaboration requests")
  }

  @Usage("list stored collaboration requests for company id")
  @Command
  void storedRequestsForCompany(@Usage("companyId") @Required @Argument Long companyId) {
    printRequests(apiSession().collaborationRequests().listStoredRequestsForCompany(companyId), "Stored collaboration requests")
  }

  @Usage("fetch notes for all collaboration requests")
  @Command
  void notesAll() {
    printNotes(apiSession().caseNotes().listNotesForAllRequests(), "Notes")
  }

  @Usage("fetch notes for one request token")
  @Command
  void notesForToken(@Usage("token") @Required @Argument String token) {
    printNotes(apiSession().caseNotes().listNotesForRequest(token), "Notes")
  }

  @Usage("list notes stored in SQLite")
  @Command
  void storedNotes() {
    printNotes(apiSession().caseNotes().listStoredNotes(), "Stored notes")
  }

  @Usage("list stored notes for request token")
  @Command
  void storedNotesForToken(@Usage("token") @Required @Argument String token) {
    printNotes(apiSession().caseNotes().listStoredNotesForRequest(token), "Stored notes")
  }

  @Usage("fetch case responses for all collaboration requests")
  @Command
  void responsesAll() {
    printResponses(apiSession().caseResponses().listResponsesForAllRequests(), "Case responses")
  }

  @Usage("fetch case responses for one request token")
  @Command
  void responsesForToken(@Usage("token") @Required @Argument String token) {
    printResponses(apiSession().caseResponses().listResponsesForRequest(token), "Case responses")
  }

  @Usage("list case responses stored in SQLite")
  @Command
  void storedResponses() {
    printResponses(apiSession().caseResponses().listStoredResponses(), "Stored case responses")
  }

  @Usage("list stored responses for request token")
  @Command
  void storedResponsesForToken(@Usage("token") @Required @Argument String token) {
    printResponses(apiSession().caseResponses().listStoredResponsesForRequest(token), "Stored case responses")
  }

  @Usage("fetch current user from Connect API")
  @Command
  void me() {
    def user = apiSession().users().getCurrentUser()
    out << "companyId=${user.companyId()} companyName=${user.companyName()} userId=${user.userId()} username=${user.username()} email=${user.email()}\n"
  }

  @Usage("show current user stored in SQLite")
  @Command
  void storedMe() {
    def users = apiSession().users().listStoredUsers()
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
    printWebhooks(apiSession().webhooks().listSubscriptions(), "Webhook subscriptions")
  }

  @Usage("list webhook subscriptions stored in SQLite")
  @Command
  void storedWebhooks() {
    printWebhooks(apiSession().webhooks().listStoredSubscriptions(), "Stored webhook subscriptions")
  }

  @Usage("search partners")
  @Command
  void partners(@Usage("searchTerm") @Required @Argument String searchTerm) {
    printPartners(apiSession().partners().searchPartners(searchTerm), "Partners")
  }

  @Usage("list partners stored in SQLite")
  @Command
  void storedPartners() {
    printPartners(apiSession().partners().listStoredPartners(), "Stored partners")
  }

  @Usage("list stored partners for search term")
  @Command
  void storedPartnersForSearch(@Usage("searchTerm") @Required @Argument String searchTerm) {
    printPartners(apiSession().partners().listStoredPartnersForSearchTerm(searchTerm), "Stored partners")
  }

  @Usage("fetch collaboration request form for receiver company")
  @Command
  void form(@Usage("companyId") @Required @Argument Long companyId) {
    def form = apiSession().collaborationRequests().getCreateForm(companyId)
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
    def created = apiSession().collaborationRequests().createRequest(companyId, caseNumber, summary, description)
    printRequests([created], "Created collaboration request")
    def requests = apiSession().collaborationRequests().listRequests()
    if (requests.any { it.id() == created.id() }) {
      out << "New request is present in collaboration requests list.\n"
    }
  }

  @Usage("sync requests, notes, and responses for all requests")
  @Command
  void sync() {
    apiSession().collaborationRequests().syncAllDetails()
    out << "Sync completed\n"
    printNotes(apiSession().caseNotes().listStoredNotes(), "Stored notes")
    printResponses(apiSession().caseResponses().listStoredResponses(), "Stored case responses")
  }

  @Usage("alias for storedRequests")
  @Command
  void stored_requests() {
    storedRequests()
  }

  @Usage("alias for storedNotes")
  @Command
  void stored_notes() {
    storedNotes()
  }

  @Usage("alias for storedResponses")
  @Command
  void stored_responses() {
    storedResponses()
  }

  @Usage("alias for storedMe")
  @Command
  void stored_me() {
    storedMe()
  }

  @Usage("alias for storedWebhooks")
  @Command
  void stored_webhooks() {
    storedWebhooks()
  }

  @Usage("alias for storedPartners")
  @Command
  void stored_partners() {
    storedPartners()
  }

  @Usage("alias for notesAll")
  @Command
  void notes() {
    notesAll()
  }

  @Usage("alias for responsesAll")
  @Command
  void responses() {
    responsesAll()
  }

  @Usage("create collaboration request (alias for createRequest)")
  @Command
  void create_request(
      @Usage("companyId") @Required @Argument Long companyId,
      @Usage("caseNumber") @Required @Argument String caseNumber,
      @Usage("summary") @Required @Argument String summary,
      @Usage("description") @Required @Argument String description
  ) {
    createRequest(companyId, caseNumber, summary, description)
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
