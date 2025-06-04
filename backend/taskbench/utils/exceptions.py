class BaseError(Exception):
    def __init__(self, message):
        self.message = message
        super().__init__(self.message)

    def __str__(self):
        return self.message


class AuthenticationError(BaseError):
    pass

class NotFound(BaseError):
    pass

class AlreadyExists(BaseError):
    pass

class YooKassaError(BaseError):
    pass