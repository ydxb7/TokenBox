# TokenBox
![image](d1.png)
![image](d2.png)
![image](d3.png)

```
graph TD
WalletFragment -->|LiveData|WalletViewModel 
WalletViewModel --> |LiveData| TransactionRepository
TransactionRepository --> |get history and balance|WalletDataSource
WalletDataSource --> |get LiveData|RoomDatabase
WalletDataSource --> |refresh database|Webservice
```

```
graph TD
SendTransactionFragment -->|LiveData|SendTransactionViewModel 
SendTransactionViewModel --> TransactionRepository
TransactionRepository --> |send transaction, get gasPrice| WalletDataSource
WalletDataSource --> |insert pendingTransaction|RoomDatabase
WalletDataSource --> |send transaction, get gasPrice|web3j
```

```
graph TD

ImportWalletFragment -->ImportWalletViewModel 
ImportWalletViewModel --> TransactionRepository
TransactionRepository --> |create and save wallet| WalletDataSource
WalletDataSource --> |save wallet|Preference
```

![image](DualPhoneTransactionDemo.gif)