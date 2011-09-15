<%@ include file="header.jsp" %>
<body>
	<div>
	   ${tou.content}
	</div>

    <div>
        <form action="" method="post" >
            <input type="checkbox" id="accept" name="accept" value="true"/>
            <label for="accept"><fmt:message key="accept"/></label>
            <button type="submit"><fmt:message key="confirm"/></button>
        </form>
    </div>
    
</body>
<%@ include file="footer.jsp" %>