def singleton(cls):
    _instance = None

    def wrapper(*args, **kwargs):
        nonlocal _instance
        if _instance is None:
            _instance = cls(*args, **kwargs)
        return _instance
    return wrapper