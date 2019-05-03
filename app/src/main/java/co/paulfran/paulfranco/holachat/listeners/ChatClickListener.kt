package co.paulfran.paulfranco.holachat.listeners

interface ChatClickListener {

    fun onChatClicked(name: String?, otheruserId: String?, chatImageUrl: String?, chatName: String?)

}